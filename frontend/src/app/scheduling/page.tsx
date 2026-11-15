
import BusinessHoursManager from "@/components/scheduling/BusinessHoursManager";
import WorkingHoursManager from "@/components/scheduling/WorkingHoursManager";

import { getBusinessHours } from "@/lib/api/scheduling";
import { getEmployees } from "@/lib/api/employees";

export default async function SchedulingPage() {
  const [businessHours, employees] = await Promise.all([
    getBusinessHours(),
    getEmployees(),
  ]);

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

          <WorkingHoursManager employees={employees} />
        </div>
      </div>
    </main>
  );
}