import { Language } from "~/types/CodeSnippetTypes";

export function getDefaultSolutionForLanguage(language: Language): string {
  switch (language) {
    case Language.JS:
      return `console.log("Hello, JavaScript!");`;
    case Language.PYTHON:
      return `print("Hello, Python!")`;
    case Language.JAVA:
      return `public class Main {\n    public static void main(String[] args) {\n        System.out.println("Hello, Java!");\n    }\n}`;
    default:
      return "";
  }
}
