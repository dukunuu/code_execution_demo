import { Language, type CodeExecutionRequest, type CodeExecutionResponse, type CodeSnippet } from "~/types/CodeSnippetTypes";
import type { Route } from "./+types/create";
import { DRAFT_KEY } from "~/constants/storage-keys";
import { fetchApi } from "~/utils/fetchData";
import { CODE_PROBLEM, CODE_SNIPPETS, EXECUTE_CODE } from "~/constants/api-reqests";
import { getDefaultSolutionForLanguage } from "~/utils/code";
import MarkdownPreview from '@uiw/react-markdown-preview';
import { Editor } from "@monaco-editor/react";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import { redirect, useSubmit } from "react-router";
import { useCallback, useEffect, useMemo, useState } from "react";
import type { ShouldRevalidateFunctionArgs, SubmitOptions } from "react-router";
import { Button } from "~/components/ui/button";
import { Select, SelectContent, SelectItem, SelectValue } from "~/components/ui/select";
import { SelectTrigger } from "@radix-ui/react-select";

type DraftSnippet = Pick<CodeSnippet, 'name' | 'language' | 'solution' | 'problemMarkdown'>

type LoaderResponse = {error: string} | DraftSnippet

type ProblemResponse = {problem: string, title: string}

type ClientActionType = 'execute' | 'save' | 'refetch'

const fetchProblem = async () => {
  localStorage.removeItem(DRAFT_KEY);
  const problem = await fetchApi<ProblemResponse>(CODE_PROBLEM)

  if ("error" in problem) {
    return problem
  }

  return {
    name: problem.title,
    language: Language.JS,
    solution: getDefaultSolutionForLanguage(Language.JS),
    problemMarkdown: problem.problem
  }
} 

export async function clientLoader({}: Route.ClientLoaderArgs): Promise<LoaderResponse> {
  const draftString = localStorage.getItem(DRAFT_KEY);
  const draftedItem: CodeSnippet = draftString ? JSON.parse(draftString) : null;
  if(draftedItem) return draftedItem;
  
  return await fetchProblem()
}

export const executeCode = async (data: FormData) => {
  const code = data.get("code")
  const language = data.get("language")

  if (!code || !language) return { error: 'Буруу оролт' }
  
  const body: CodeExecutionRequest = {
    code: code as string,
    language: language as Language
  }

  const res = await fetchApi<CodeExecutionResponse>(EXECUTE_CODE, {
    method: "POST",
    headers: {
      "Content-type": "application/json"
    },
    body: JSON.stringify(body)
  })

  return res
} 

const saveCode = async (data: FormData) => {
  const language = data.get("language")?.toString();
  const name = data.get("name")?.toString();
  const problemMarkdown = data.get("problemMarkdown")?.toString();
  const solution = data.get("solution")?.toString();
  
  if (!language || !Object.values(Language).includes(language as Language)) {
    return { error: "Invalid or missing programming language" };
  }
  
  if (!name?.trim()) {
    return { error: "Name is required" };
  }
  
  if (!solution?.trim()) {
    return { error: "Solution code is required" };
  }
  
  const body: Partial<CodeSnippet> = {
    language: language as Language,
    name,
    problemMarkdown: problemMarkdown || "",
    solution
  };

  const response = await fetchApi<CodeSnippet>(CODE_SNIPPETS, {
    method: 'POST',
    headers: {
      'Content-type': 'application/json'
    },
    body: JSON.stringify(body)
  })

  if ('error' in response) {
    return response
  }
  localStorage.removeItem(DRAFT_KEY)
  return redirect('/')
}

clientLoader.hydrate = true as const;

export async function clientAction({request}: Route.ClientActionArgs): Promise<CodeExecutionResponse|{error:string}|undefined | DraftSnippet | Response> {
  const data = await request.formData()

  const type = data.get("type") as ClientActionType

  switch (type) {
    case "execute":
      return await executeCode(data)
    case "save":
      return await saveCode(data)
    case "refetch":
      return await fetchProblem()
    default:
      return { error: "Буруу оролт" }
  }
}

export function shouldRevalidate({}: ShouldRevalidateFunctionArgs) {
  return false;
}

export function HydrateFallback() {
  return (
    <div className="min-h-screen w-full flex flex-col justify-center items-center text-2xl font-bold text-blue-700">
      Шинэ бодлого үүсгэж байна...
    </div>
  );
}

const getEnumValues = <T extends object>(enumObject: T): Array<T[keyof T]> => {
  return Object.values(enumObject);
}

export default function CreatePage({actionData, loaderData}: Route.ComponentProps) {
  const submit = useSubmit()

  const submitArgs: SubmitOptions = {
    action: '/create',
    method: 'POST',
  }

  const [name, setName] = useState("")
  const [problemMarkdown, setProblem] = useState("")
  const [solution, setSolution] = useState("")
  const [language, setLanguage] = useState(() => {
    if (loaderData && !("error" in loaderData)) {
      return loaderData.language;
    }
    
    return Language.JS;
  });
  const [loading, setLoading] = useState(false)
  const [executing, setExecuting] = useState(false)
  const [saving, setSaving] = useState(false)

  const isDisabled = useMemo(() =>loading || executing || saving, [loading, executing, saving])

  const handleRefresh = useCallback(async () => {
    setLoading(true)
    await submit({type: 'refetch'}, submitArgs)
    setLoading(false)
  }, [])

  const executeCode = useCallback(async () => {
    setExecuting(true)
    await submit({
      code: solution,
      language,
      type: 'execute'
    }, submitArgs)
    setExecuting(false)
  }, [submit, solution, language])

  const saveSnippet = useCallback(async () => {
    setSaving(true)
    await submit({
      name: name,
      solution,
      language,
      problemMarkdown,
      type: 'save'
    }, submitArgs)
    setSaving(false)
  }, [name, solution, language, problemMarkdown])

  useEffect(() => {
    if (!loaderData || ("error" in loaderData)) return
    const { name, problemMarkdown, solution, language } = loaderData
    setName(name)
    setProblem(problemMarkdown) 
    setSolution(solution)
    setLanguage(language)

    const draft: DraftSnippet = {
      name,
      problemMarkdown,
      solution,
      language
    } 

    localStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
  }, [loaderData])

  useEffect(() => {
    if (!actionData) return
    if (("error" in actionData || "output" in actionData)) return
    
    const { name, problemMarkdown, solution, language } = actionData
    setName(name)
    setProblem(problemMarkdown) 
    setSolution(solution)
    setLanguage(language)

    const draft: DraftSnippet = {
      name,
      problemMarkdown,
      solution,
      language
    } 

    localStorage.setItem(DRAFT_KEY, JSON.stringify(draft))
  }, [actionData])

  useEffect(() => {
    setSolution(getDefaultSolutionForLanguage(language))
  }, [language])

  if(loaderData && typeof loaderData !== "string" && !localStorage.getItem(DRAFT_KEY)) {
    localStorage.setItem(DRAFT_KEY, JSON.stringify(loaderData))
  }

  if (typeof loaderData === "string") {
    return <div className="w-full h-dvh">{loaderData}</div>;
  }

  return (
    <div className="flex w-full h-dvh">
      <PanelGroup direction="horizontal" className="w-full h-full">
        <Panel defaultSize={50} minSize={20}>
          <div className={`h-full ${loading ? 'overflow-none' : 'overflow-auto'} relative bg-gray-900`}>
            <MarkdownPreview 
              source={problemMarkdown} 
              className="flex flex-col w-full p-5" 
            />
            {loading && (
              <div className="bg-gray-800/70 text absolute top-0 animate-pulse w-full h-full text-blue-500 text-2xl font-bold flex items-center justify-center">
                Шинэ бодлого үүсгэж байна
              </div>
            )}
          </div>
        </Panel>
        <PanelResizeHandle className="w-2 bg-gray-900 hover:bg-gray-400 transition-colors" />
        <Panel defaultSize={50} minSize={20}>
          <PanelGroup direction="vertical" className="h-full w-full">
            <Panel defaultValue={60} minSize={20}>
              <div className="bg-gray-800 p-3 flex justify-between">
                <Button className="bg-blue-700 px-5 py-3 disabled:opacity-75 rounded-md" disabled={isDisabled} onClick={handleRefresh}>
                  Шинэ бодлого
                </Button>
                <div className="flex gap-3">
                  <Select value={language} onValueChange={(e) => setLanguage(e as Language)}>
                    <SelectTrigger  className="px-5 text-white border-1 rounded-md py-1 border-gray-400">
                      <SelectValue defaultValue={Language.JS} />
                    </SelectTrigger>
                    <SelectContent className="bg-gray-800">
                      {getEnumValues(Language).map((val) => (
                        <SelectItem key={val} value={val} className="hover:bg-gray-700 text-white">
                          {val}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <Button className="bg-blue-700 px-5 py-3 disabled:opacity-75  rounded-md" disabled={isDisabled} onClick={executeCode}>
                    Ажлуулах
                  </Button>
                  <Button className="bg-green-700 px-5 py-3 disabled:opacity-75  rounded-md" disabled={isDisabled} onClick={saveSnippet}>
                    Хадгалах
                  </Button>
                </div>
              </div>
              <Editor 
                language={language} 
                value={solution}
                theme="vs-dark" 
                className="w-full h-full bg-gray-900"
                onChange={(e) => e && setSolution(e)}
              />
            </Panel>
            <PanelResizeHandle className="h-2 bg-gray-900 hover:bg-gray-400 transition-colors" />
            <Panel defaultSize={40} minSize={20}>
              {actionData ? (
                <div className="p-4 bg-gray-800 h-full overflow-auto font-mono text-sm">
                  {/* Handle simple error object */}
                  {actionData && "error" in actionData && (
                    <div className="text-red-500 whitespace-pre-wrap">
                      <div className="font-bold mb-2">Error:</div>
                      {actionData.error}
                    </div>
                  )}
                  
                  {"output" in actionData && actionData.output !== undefined && actionData.output !== "" && (
                    <>
                      {/* Show output if exists */}
                      {actionData.output && (
                        <div className="mb-4 whitespace-pre-wrap">
                          <div className="font-bold text-gray-700 mb-1">Output:</div>
                          <div className="bg-black/80 text-white p-3 rounded">{actionData.output}</div>
                        </div>
                      )}
                      
                      {/* Show exit code */}
                      <div className={`text-sm ${actionData.exitCode === 0 ? 'text-green-600' : 'text-red-600'}`}>
                        Exit Code: {actionData.exitCode}
                        {actionData.timeout && <span className="ml-2 bg-yellow-100 px-2 py-1 rounded text-yellow-800">Timeout</span>}
                      </div>
                    </>
                  )}
                </div>
              ) : (
                <div className="h-full flex bg-gray-800 items-center justify-center text-gray-200">
                  Run your code to see the output here
                </div>
              )}
            </Panel>
          </PanelGroup>
        </Panel>
      </PanelGroup>
    </div>
  );
}
