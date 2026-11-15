
"use client";

import { useEffect, useState, type FormEvent } from "react";

import type { Employee } from "@/lib/api/employees";

import {
  type EmployeeWorkingHours,
  getEmployeeWorkingHours,
  createEmployeeWorkingHours,
  updateEmployeeWorkingHours,
  deleteEmployeeWorkingHours,
} from "@/lib/api/scheduling";

interface Props {
  employees: Employee[];
}

const DAYS = [
  "",
  "Monday",
  "Tuesday",
  "Wednesday",
  "Thursday",
  "Friday",
  "Saturday",
  "Sunday",
];

function EmployeeShiftEditor({
  employeeId,
}: {
  employeeId: number;
}) {
  const [shifts, setShifts] = useState<EmployeeWorkingHours[]>([]);
  const [loading, setLoading] = useState(true);

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  const [dayOfWeek, setDayOfWeek] = useState(1);
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("17:00");

  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const [errorMessage, setErrorMessage] = useState("");
  const [message, setMessage] = useState("");

  // Load the selected employee's recurring shifts.
  useEffect(() => {
    let cancelled = false;

    getEmployeeWorkingHours(employeeId)
      .then((data) => {
        if (!cancelled) {
          setShifts(data);
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setErrorMessage(
            error instanceof Error
              ? error.message
              : "Failed to load shifts",
          );
        }
      })
      .finally(() => {
        if (!cancelled) {
          setLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [employeeId]);

  function beginAdd() {
    setEditingId(null);
    setDayOfWeek(1);
    setStartTime("09:00");
    setEndTime("17:00");
    setErrorMessage("");
    setMessage("");
    setFormOpen(true);
  }

  function beginEdit(shift: EmployeeWorkingHours) {
    setEditingId(shift.id);
    setDayOfWeek(shift.dayOfWeek);
    setStartTime(shift.startTime.slice(0, 5));
    setEndTime(shift.endTime.slice(0, 5));
    setErrorMessage("");
    setMessage("");
    setFormOpen(true);
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    setSaving(true);
    setErrorMessage("");
    setMessage("");

    const request = {
      dayOfWeek,
      startTime,
      endTime,
    };

    try {
      if (editingId === null) {
        await createEmployeeWorkingHours(employeeId, request);
      } else {
        await updateEmployeeWorkingHours(
          employeeId,
          editingId,
          request,
        );
      }

      const updatedShifts =
        await getEmployeeWorkingHours(employeeId);

      setShifts(updatedShifts);
      setFormOpen(false);
      setMessage("Employee shift saved.");
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to save shift",
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(shiftId: number) {
    if (!window.confirm("Delete this weekly shift?")) {
      return;
    }

    setDeletingId(shiftId);
    setErrorMessage("");
    setMessage("");

    try {
      await deleteEmployeeWorkingHours(employeeId, shiftId);

      setShifts((current) =>
        current.filter((shift) => shift.id !== shiftId),
      );

      if (editingId === shiftId) {
        setEditingId(null);
        setFormOpen(false);
      }

      setMessage("Employee shift deleted.");
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to delete shift",
      );
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div className="mt-6">
      <button
        type="button"
        onClick={beginAdd}
        disabled={loading || saving}
        className="rounded-lg bg-neutral-900 px-4 py-2 text-white disabled:opacity-50"
      >
        Add shift
      </button>

      {message && (
        <p role="status" className="mt-3 text-sm text-green-700">
          {message}
        </p>
      )}

      {errorMessage && (
        <p role="alert" className="mt-3 text-sm text-red-600">
          {errorMessage}
        </p>
      )}

      {formOpen && (
        <form
          onSubmit={handleSubmit}
          className="mt-5 rounded-lg border border-neutral-200 bg-neutral-50 p-4"
        >
          <h3 className="font-medium">
            {editingId === null ? "Add shift" : "Edit shift"}
          </h3>

          <div className="mt-4 grid gap-4 sm:grid-cols-3">
            <label className="grid gap-2">
              <span className="text-sm font-medium">
                Day of week
              </span>

              <select
                value={dayOfWeek}
                onChange={(event) =>
                  setDayOfWeek(Number(event.target.value))
                }
                className="rounded-lg border border-neutral-300 bg-white p-2"
              >
                {DAYS.slice(1).map((day, index) => (
                  <option key={day} value={index + 1}>
                    {day}
                  </option>
                ))}
              </select>
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium">
                Start time
              </span>

              <input
                type="time"
                value={startTime}
                onChange={(event) =>
                  setStartTime(event.target.value)
                }
                required
                className="rounded-lg border border-neutral-300 bg-white p-2"
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium">
                End time
              </span>

              <input
                type="time"
                value={endTime}
                onChange={(event) =>
                  setEndTime(event.target.value)
                }
                required
                className="rounded-lg border border-neutral-300 bg-white p-2"
              />
            </label>
          </div>

          <div className="mt-4 flex flex-wrap gap-3">
            <button
              type="submit"
              disabled={saving}
              className="rounded-lg bg-neutral-900 px-4 py-2 text-white disabled:opacity-50"
            >
              {saving ? "Saving..." : "Save shift"}
            </button>

            <button
              type="button"
              onClick={() => {
                setFormOpen(false);
                setErrorMessage("");
              }}
              disabled={saving}
              className="rounded-lg border border-neutral-300 bg-white px-4 py-2 disabled:opacity-50"
            >
              Cancel
            </button>
          </div>
        </form>
      )}

      <div className="mt-6 divide-y divide-neutral-200">
        {loading ? (
          <p className="py-4 text-sm text-neutral-500">
            Loading employee shifts...
          </p>
        ) : shifts.length === 0 ? (
          <p className="py-4 text-sm text-neutral-500">
            No weekly shifts configured for this employee.
          </p>
        ) : (
          shifts.map((shift) => (
            <div
              key={shift.id}
              className="flex flex-wrap items-center justify-between gap-4 py-4"
            >
              <div>
                <h3 className="font-medium">
                  {DAYS[shift.dayOfWeek]}
                </h3>

                <p className="mt-1 text-sm text-neutral-500">
                  {shift.startTime.slice(0, 5)}
                  {" – "}
                  {shift.endTime.slice(0, 5)}
                </p>
              </div>

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => beginEdit(shift)}
                  disabled={saving || deletingId !== null}
                  className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50 disabled:opacity-50"
                >
                  Edit
                </button>

                <button
                  type="button"
                  onClick={() => handleDelete(shift.id)}
                  disabled={saving || deletingId !== null}
                  className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50 disabled:opacity-50"
                >
                  {deletingId === shift.id
                    ? "Deleting..."
                    : "Delete"}
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}

export default function WorkingHoursManager({
  employees,
}: Props) {
  const activeEmployees = employees.filter(
    (employee) => employee.active,
  );

  const [employeeId, setEmployeeId] = useState<number | null>(
    activeEmployees[0]?.id ?? null,
  );

  return (
    <section className="rounded-xl border border-neutral-200 bg-white p-6 shadow-sm">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold">
          Employee Working Hours
        </h2>

        <p className="mt-1 text-neutral-500">
          Manage recurring weekly shifts for salon employees.
        </p>
      </div>

      {activeEmployees.length === 0 ? (
        <p className="text-neutral-500">
          No active employees available.
        </p>
      ) : (
        <>
          <label className="grid max-w-sm gap-2">
            <span className="text-sm font-medium">
              Select employee
            </span>

            <select
              value={employeeId ?? ""}
              onChange={(event) =>
                setEmployeeId(Number(event.target.value))
              }
              className="rounded-lg border border-neutral-300 bg-white p-2"
            >
              {activeEmployees.map((employee) => (
                <option key={employee.id} value={employee.id}>
                  {employee.displayName ||
                    `${employee.firstName} ${employee.lastName}`}
                </option>
              ))}
            </select>
          </label>

          {employeeId !== null && (
            <EmployeeShiftEditor
              key={employeeId}
              employeeId={employeeId}
            />
          )}
        </>
      )}
    </section>
  );
}