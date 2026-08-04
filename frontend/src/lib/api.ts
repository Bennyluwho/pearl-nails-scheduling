export type BackendHealth = {
  status: string;
  service: string;
  timestamp: string;
};

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

export async function getBackendHealth(): Promise<BackendHealth> {
  const response = await fetch(`${API_BASE_URL}/health`, {
    cache: "no-store",
  });

  if (!response.ok) {
    throw new Error(
      `Backend health request failed with status ${response.status}`,
    );
  }

  return response.json() as Promise<BackendHealth>;
}