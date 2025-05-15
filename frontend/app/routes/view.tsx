import { fetchApi } from "~/utils/fetchData";
import type { Route } from "./+types/view";
import type { CodeSnippet } from "~/types/CodeSnippetTypes";
import { CODE_SNIPPETS } from "~/constants/api-reqests";
import { Panel, PanelGroup, PanelResizeHandle } from "react-resizable-panels";
import { Editor } from "@monaco-editor/react";
import { executeCode } from "./create";
import { useFetcher } from "react-router";
import MarkdownPreview from "@uiw/react-markdown-preview"
import { useCallback } from "react";
import { Button } from "~/components/ui/button";
import { useSubmit } from "react-router";

const deleteSnippet = async (id: number) => {
  const res = await fetchApi(`${CODE_SNIPPETS}/${id}`, {
    method: "DELETE"
  })
}

export async function loader({params}: Route.LoaderArgs) {
  const snippet = await fetchApi<CodeSnippet>(`${CODE_SNIPPETS}/${params.snippetId}`)
  return snippet
}

export async function action({params, request}: Route.ActionArgs) {
  const method = request.method
  const formData = await request.formData()
  switch (method) {
    case "DELETE":
      return await deleteSnippet(parseInt(params.snippetId))
    case "POST":
      return await executeCode(formData)
  }
}

export default function SnippetView({actionData, params, loaderData}: Route.ComponentProps) {
  const submit = useSubmit()

  const executeCode = useCallback(async () => {
    if ('error' in loaderData){
      return
    }
    await submit({
      code: loaderData.solution,
      language: loaderData.language,
    }, {
        action: `/${params.snippetId}`,
        method: 'POST'
      }) 
  }, [submit])
  if ('error' in loaderData){
    return <></>
  }
  const {problemMarkdown, language, solution} = loaderData
  return (
    <div className="flex w-full h-dvh">
      <PanelGroup direction="horizontal" className="w-full h-full">
        <Panel defaultSize={50} minSize={20}>
          <div className={`h-full overflow-auto relative bg-gray-900`}>
            <MarkdownPreview 
              source={problemMarkdown} 
              className="flex flex-col w-full p-5" 
            />
          </div>
        </Panel>
        <PanelResizeHandle className="w-2 bg-gray-900 hover:bg-gray-400 transition-colors" />
        <Panel defaultSize={50} minSize={20}>
          <PanelGroup direction="vertical" className="h-full w-full">
            <Panel defaultValue={60} minSize={20}>
              <div className="bg-gray-800 p-3 flex justify-between">
                  <Button className="bg-blue-700 px-5 py-3 disabled:opacity-75  rounded-md" onClick={executeCode}>
                    Ажлуулах
                  </Button>
                </div>
              <Editor 
                language={language} 
                value={solution}
                theme="vs-dark" 
                options={{
                  readOnly: true,
                }}  
                className="w-full h-full bg-gray-900"
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
  )
}
