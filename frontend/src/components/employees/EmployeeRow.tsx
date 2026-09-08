"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

import {
  deactivateEmployee,
  Employee,
  updateEmployee,
} from "@/lib/api/employees";

import EditEmployeeForm from "./EditEmployeeForm";

interface EmployeeRowProps {
  employee: Employee;
}

export default function EmployeeRow({
  employee,
}: EmployeeRowProps) {
  const router = useRouter();

  const [editing, setEditing] = useState(false);
  const [changingStatus, setChangingStatus] =
    useState(false);

  const [statusError, setStatusError] =
    useState("");

  async function handleStatusChange() {
    setChangingStatus(true);
    setStatusError("");

    try {
      if (employee.active) {
        await deactivateEmployee(employee.id);
      } else {
        await updateEmployee(
          employee.id,
          {
            active: true,
          },
        );
      }

      router.refresh();
    } catch (error) {
      setStatusError(
        error instanceof Error
          ? error.message
          : "Failed to update employee",
      );
    } finally {
      setChangingStatus(false);
    }
  }

  const employeeName =
    employee.displayName ||
    `${employee.firstName} ${employee.lastName}`;

  if (editing) {
    return (
      <div className="py-4">
        <h3 className="font-medium">Editing {employeeName}</h3>
        <EditEmployeeForm
          employee={employee}
          onCancel={() => setEditing(false)}
        />
      </div>
    );
  }

  return (
    <div className="py-4">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between sm:gap-6">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-3">
            <h3 className="break-words font-medium">{employeeName}</h3>

            {!employee.active && (
              <span className="rounded-full bg-neutral-100 px-2 py-0.5 text-xs text-neutral-600">
                Inactive
              </span>
            )}
          </div>

          <p className="mt-1 text-sm text-neutral-500">
            {employee.firstName} {employee.lastName}
          </p>

          {(employee.phone || employee.email) && (
            <div className="mt-2 space-y-1 text-sm text-neutral-500">
              {employee.phone && <p>{employee.phone}</p>}
              {employee.email && (
                <p className="break-words">{employee.email}</p>
              )}
            </div>
          )}
        </div>

        <div className="flex shrink-0 flex-wrap gap-2">
          <button
            type="button"
            onClick={() => setEditing(true)}
            className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50"
          >
            Edit
          </button>

          <button
            type="button"
            disabled={changingStatus}
            onClick={handleStatusChange}
            className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50 disabled:opacity-50"
          >
            {changingStatus
              ? "Updating..."
              : employee.active
                ? "Deactivate"
                : "Reactivate"}
          </button>
        </div>
      </div>

      {statusError && (
        <p role="alert" className="mt-2 text-sm text-red-600">
          {statusError}
        </p>
      )}
    </div>
  );
}
