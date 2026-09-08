import CreateEmployeeForm from "@/components/employees/CreateEmployeeForm";
import EmployeeRow from "@/components/employees/EmployeeRow";
import { getEmployees } from "@/lib/api/employees";

export default async function EmployeesPage() {
  const employees = await getEmployees();

  return (
    <main className="min-h-screen bg-neutral-50 p-8 text-neutral-900">
      <div className="mx-auto max-w-6xl">
        <header className="mb-10">
          <p className="text-sm font-medium uppercase tracking-widest text-neutral-500">
            Pearl Nails
          </p>

          <h1 className="mt-2 text-4xl font-semibold tracking-tight">
            Employees
          </h1>

          <p className="mt-3 text-neutral-600">
            Manage salon employees and staff information.
          </p>
        </header>

        <div className="mb-10">
          <CreateEmployeeForm />
        </div>

        <section className="rounded-xl border border-neutral-200 bg-white p-6 shadow-sm">
          <div className="mb-6">
            <h2 className="text-2xl font-semibold">Salon team</h2>
            <p className="mt-1 text-neutral-500">
              Update employee details and availability.
            </p>
          </div>

          {employees.length === 0 ? (
            <p className="text-neutral-500">
              No employees have been added yet.
            </p>
          ) : (
            <div className="divide-y divide-neutral-200">
              {employees.map((employee) => (
                <EmployeeRow key={employee.id} employee={employee} />
              ))}
            </div>
          )}
        </section>
      </div>
    </main>
  );
}
