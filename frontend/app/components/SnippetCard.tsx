import { useCallback } from "react";
import { Link, useFetcher, useSubmit } from "react-router";
import type { CodeSnippet } from "~/types/CodeSnippetTypes";
import { Button } from "./ui/button";
import { Trash } from "lucide-react";

interface SnippetCardProps {
  snippet: CodeSnippet;
}

export function SnippetCard({ snippet }: SnippetCardProps) {
  const fetcher = useFetcher()

  const deleteSnippet = useCallback(async (e: React.MouseEvent) => {
    e.stopPropagation()
    console.log(snippet.id)
    await fetcher.submit(snippet.id, {
      action: `/${snippet.id}`,
      method: "DELETE"
    })
  }, [])

  const formattedDate = new Date(snippet.createdAt).toLocaleDateString(
    undefined,
    {
      year: "numeric",
      month: "long",
      day: "numeric",
    },
  );

  const markdownPreview =
    snippet.problemMarkdown.substring(0, 100) +
    (snippet.problemMarkdown.length > 100 ? "..." : "");

  return (
    <Link
      to={`/${snippet.id}`}
      className="group bg-white rounded-lg shadow-md hover:shadow-xl focus:shadow-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-opacity-50 transition-all duration-300 ease-in-out p-6 flex flex-col h-full"
    >
      <div className="mb-3">
        <div className="flex w-full justify-between">
          <h3 className="text-xl font-semibold text-blue-600 group-hover:text-blue-700 transition-colors duration-200 mb-1.5 truncate">
            {snippet.name}
          </h3>
          <Button variant={'destructive'} onClick={deleteSnippet}>
            <Trash />
          </Button>
        </div>
        <span className="text-xs font-medium text-white bg-slate-500 group-hover:bg-slate-600 transition-colors duration-200 inline-block px-2.5 py-1 rounded-full">
          {typeof snippet.language === "string"
            ? snippet.language.toUpperCase()
            : "N/A"}
        </span>
      </div>

      <p
        className="text-sm text-gray-700 mb-4 flex-grow"
        title={snippet.problemMarkdown}
      >
        {markdownPreview}
      </p>

      <div className="border-t border-gray-200 pt-3 mt-auto">
        {" "}
        <div className="flex justify-between items-center text-xs text-gray-500">
          <span>
            Comments:{" "}
            <span className="font-semibold text-gray-700">
              {snippet.comments?.length || 0}
            </span>
          </span>
          <span className="font-medium text-gray-600">{formattedDate}</span>
        </div>
      </div>
    </Link>
  );
}

