import { fetchApi } from "~/utils/fetchData";
import type { Route } from "./+types/home";
import { SnippetCard } from "~/components/SnippetCard";
import { useCallback, useMemo, useState } from "react";
import { useLoaderData } from "react-router";
import { Link } from "react-router";
import type { CodeSnippet } from "~/types/CodeSnippetTypes";
import { CODE_SNIPPETS } from "~/constants/api-reqests";
import { useSubmit } from "react-router";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Code share" },
    { name: "description", content: "Өөрийн бичсэн кодоо хуваалцана уу" },
  ];
}

interface LoaderData {
  snippets: CodeSnippet[];
  error?: string;
}

export async function loader({}: Route.LoaderArgs): Promise<LoaderData> {
  try {
    const result = await fetchApi<CodeSnippet[] | { error: string }>(
      CODE_SNIPPETS,
    );

    if (result && Array.isArray(result)) {
      return { snippets: result };
    } else if (result && "error" in result) {
      console.error("Error fetching snippets:", result.error);
      return { snippets: [], error: String(result.error) };
    }
    return { snippets: [] }; // Default empty if unexpected response
  } catch (error) {
    console.error("Failed to load snippets:", error);
    return {
      snippets: [],
      error:
        error instanceof Error ? error.message : "An unknown error occurred",
    };
  }
}

export default function Home() {
  const { snippets, error } = useLoaderData<typeof loader>();
  const [searchTerm, setSearchTerm] = useState("");

  const filteredSnippets = useMemo(() => {
    if (!snippets) return [];
    if (!searchTerm.trim()) {
      return snippets;
    }
    const lowerSearchTerm = searchTerm.toLowerCase();
    return snippets.filter(
      (snippet) =>
        snippet.name.toLowerCase().includes(lowerSearchTerm) ||
        snippet.problemMarkdown.toLowerCase().includes(lowerSearchTerm) ||
        (typeof snippet.language === "string" &&
          snippet.language.toLowerCase().includes(lowerSearchTerm)),
    );
  }, [snippets, searchTerm]);

  if (error) {
    return (
      <div className="flex flex-col items-center justify-center min-h-screen bg-red-50 p-4 text-center">
        <h2 className="text-2xl font-semibold text-red-700 mb-2">
          Error loading snippets
        </h2>
        <p className="text-red-600">{error}</p>
      </div>
    );
  }

  if (!snippets) {
    return (
      <div className="flex items-center justify-center min-h-screen text-gray-500 text-xl">
        Loading snippets or none found...
      </div>
    );
  }

  return (
    <div className="container relative mx-auto min-h-screen px-4 py-8">
      <header className="mb-8 text-center">
        <h1 className="text-4xl font-bold text-gray-800 mb-6">
          Кодын хэсгүүд {/* Code Snippets */}
        </h1>
        <div className="max-w-xl mx-auto">
          <input
            type="text"
            placeholder="Кодын хэсгүүдийг нэр, агуулга, эсвэл хэлээр нь хайх..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full px-4 text-black py-3 border border-gray-300 rounded-full shadow-sm focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition-shadow"
            aria-label="Кодын хэсгүүд хайх"
          />
        </div>
      </header>

      {filteredSnippets.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredSnippets.map((snippet) => (
            <SnippetCard key={snippet.id} snippet={snippet} />
          ))}
        </div>
      ) : (
        <div className="text-center text-gray-500 py-10 text-xl">
          {searchTerm
            ? "Таны хайлтад тохирох кодын хэсэг олдсонгүй."
            : "Одоогоор кодын хэсэг алга байна. Та хамгийн түрүүнд нэмээрэй!"
            }
        </div>
      )}

      <Link className="fixed bottom-10 right-10" to='/create'>
        <button className="bg-blue-700 text-white shadow-xl rounded-full font-bold text-xl px-8 py-7">
          Нэмэх
        </button>
      </Link>

    </div>
  );
}
