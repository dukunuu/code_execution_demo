const API_URL = import.meta.env.VITE_API_URL as string || ""

export async function fetchApi<T>(route: string, opts?: RequestInit){
  const res = await fetch(`${API_URL}/${route}`, opts)

  if (!res.ok) {
    return {
      error: `${route} returned an error.`
    }; 
  }

  return await res.json() as T;
}
