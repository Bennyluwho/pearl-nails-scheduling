
import BusinessHoursManager from "@/components/scheduling/BusinessHoursManager";
import { getBusinessHours } from "@/lib/api/scheduling";

export default async function SchedulingPage() {
  const businessHours = await getBusinessHours();

  return (
    <main className="min-h-screen bg-neutral-50 p-8 text-neutral-900">
      <div className="mx-auto max-w-6xl">
        <header className="mb-10">
          <p className="text-sm font-medium uppercase tracking-widest text-neutral-500">
            Pearl Nails
          </p>

          <h1 className="mt-2 text-4xl font-semibold tracking-tight">
            Scheduling
          </h1>

          <p className="mt-3 text-neutral-600">
            Manage salon hours, employee shifts, and time off.
          </p>
        </header>

        <div className="space-y-8">
          <BusinessHoursManager hours={businessHours} />
        </div>
      </div>
    </main>
  );
}
