import { getBackendHealth } from "@/lib/api";

export default async function Home() {
  const health = await getBackendHealth().catch(() => null);

  if (!health) {
    return (
      <main className="min-h-screen bg-neutral-50 px-6 py-12 text-neutral-900">
        <div className="mx-auto max-w-4xl">
          <h1 className="text-4xl font-semibold tracking-tight">
            Pearl Nails Scheduling
          </h1>

          <section className="mt-10 rounded-xl border border-red-200 bg-red-50 p-6">
            <h2 className="font-semibold text-red-900">Backend unavailable</h2>

            <p className="mt-2 text-red-800">
              The frontend could not connect to the Spring Boot API.
            </p>
          </section>
        </div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-neutral-50 px-6 py-12 text-neutral-900">
      <div className="mx-auto max-w-4xl">
        <header className="mb-12">
          <p className="mb-2 text-sm font-medium uppercase tracking-widest text-neutral-500">
            Pearl Nails
          </p>

          <h1 className="text-4xl font-semibold tracking-tight">
            Scheduling Administration
          </h1>

          <p className="mt-4 max-w-2xl text-neutral-600">
            Internal tools for managing salon services, employees, schedules,
            and appointments.
          </p>
        </header>

        <section className="rounded-xl border border-neutral-200 bg-white p-6 shadow-sm">
          <h2 className="text-xl font-semibold">System status</h2>

          <dl className="mt-6 grid gap-4 sm:grid-cols-3">
            <div>
              <dt className="text-sm text-neutral-500">Backend</dt>
              <dd className="mt-1 font-medium">{health.service}</dd>
            </div>

            <div>
              <dt className="text-sm text-neutral-500">Status</dt>
              <dd className="mt-1 font-medium text-green-700">
                {health.status}
              </dd>
            </div>

            <div>
              <dt className="text-sm text-neutral-500">Checked</dt>
              <dd className="mt-1 font-medium">
                {new Date(health.timestamp).toLocaleString()}
              </dd>
            </div>
          </dl>
        </section>
      </div>
    </main>
  );
}
