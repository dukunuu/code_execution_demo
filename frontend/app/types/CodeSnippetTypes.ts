export enum Language {
  JS = "javascript",
  JAVA = "java",
  PYTHON = "python",
}

export interface CodeSnippet {
  id: number;
  name: string;
  problemMarkdown: string;
  solution: string;
  language: Language
  createdAt: Date
  comments: string[]
}

export interface CodeExecutionResponse {
  output: string
  error: string
  exitCode: number
  timeout: boolean
}

export interface CodeExecutionRequest {
  code: string
  language: Language
}
