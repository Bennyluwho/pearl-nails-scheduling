
"use client";

import { useEffect, useState, type FormEvent } from "react";

import type { Employee } from "@/lib/api/employees";

import {
  type EmployeeTimeOff,
  type CreateTimeOffRequest,
  getEmployeeTimeOff,
  createEmployeeTimeOff,
  updateEmployeeTimeOff,
  deleteEmployeeTimeOff,
} from "@/lib/api/scheduling";

interface Props {
  employees: Employee[];
}

function EmployeeTimeOffEditor({
  employeeId,
}: {
  employeeId: number;
}) {
  const [timeOff, setTimeOff] = useState<EmployeeTimeOff[]>([]);
  const [loading, setLoading] = useState(true);

  const [formOpen, setFormOpen] = useState(false);
  const [editingId, setEditingId] = useState<number | null>(null);

  const [date, setDate] = useState("");
  const [fullDay, setFullDay] = useState(true);
  const [startTime, setStartTime] = useState("09:00");
  const [endTime, setEndTime] = useState("17:00");
  const [reason, setReason] = useState("");

  const [saving, setSaving] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const [errorMessage, setErrorMessage] = useState("");
  const [message, setMessage] = useState("");

  // Load time off for the selected employee.
  useEffect(() => {
    let cancelled = false;

    getEmployeeTimeOff(employeeId)
      .then((data) => {
        if (!cancelled) {
          setTimeOff(data);
        }
      })
      .catch((error) => {
        if (!cancelled) {
          setErrorMessage(
            error instanceof Error
              ? error.message
              : "Failed to load time off",
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
    setDate("");
    setFullDay(true);
    setStartTime("09:00");
    setEndTime("17:00");
    setReason("");
    setErrorMessage("");
    setMessage("");
    setFormOpen(true);
  }

  function beginEdit(entry: EmployeeTimeOff) {
    setEditingId(entry.id);
    setDate(entry.timeOffDate);
    setFullDay(entry.fullDay);
    setStartTime(entry.startTime?.slice(0, 5) ?? "09:00");
    setEndTime(entry.endTime?.slice(0, 5) ?? "17:00");
    setReason(entry.reason ?? "");
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

    const request: CreateTimeOffRequest = {
      timeOffDate: date,
      fullDay,
      reason,
      // Omit clock times for full-day absences.
      ...(fullDay ? {} : { startTime, endTime }),
    };

    try {
      if (editingId === null) {
        await createEmployeeTimeOff(employeeId, request);
      } else {
        await updateEmployeeTimeOff(
          employeeId,
          editingId,
          request,
        );
      }

      const updated = await getEmployeeTimeOff(employeeId);
      setTimeOff(updated);
      setFormOpen(false);
      setMessage("Employee time off saved.");
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to save time off",
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete(timeOffId: number) {
    if (!window.confirm("Delete this time-off record?")) {
      return;
    }

    setDeletingId(timeOffId);
    setErrorMessage("");
    setMessage("");

    try {
      await deleteEmployeeTimeOff(employeeId, timeOffId);

      setTimeOff((current) =>
        current.filter((entry) => entry.id !== timeOffId),
      );

      if (editingId === timeOffId) {
        setFormOpen(false);
        setEditingId(null);
      }

      setMessage("Time-off record deleted.");
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to delete time off",
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
        disabled={loading || saving || deletingId !== null}
        className="rounded-lg bg-neutral-900 px-4 py-2 text-white disabled:opacity-50"
      >
        Add time off
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
            {editingId === null ? "Add time off" : "Edit time off"}
          </h3>

          <div className="mt-4 grid gap-4 sm:grid-cols-2">
            <label className="grid gap-2">
              <span className="text-sm font-medium">Date</span>

              <input
                type="date"
                value={date}
                onChange={(event) => setDate(event.target.value)}
                required
                className="rounded-lg border border-neutral-300 bg-white p-2"
              />
            </label>

            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={fullDay}
                onChange={(event) =>
                  setFullDay(event.target.checked)
                }
              />
              <span className="text-sm font-medium">
                Full day off
              </span>
            </label>

            {!fullDay && (
              <>
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
              </>
            )}

            <label className="grid gap-2 sm:col-span-2">
              <span className="text-sm font-medium">
                Reason (optional)
              </span>

              <input
                type="text"
                value={reason}
                onChange={(event) =>
                  setReason(event.target.value)
                }
                maxLength={255}
                placeholder="Vacation, personal day..."
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
              {saving ? "Saving..." : "Save time off"}
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
            Loading employee time off...
          </p>
        ) : timeOff.length === 0 ? (
          <p className="py-4 text-sm text-neutral-500">
            No time-off records for this employee.
          </p>
        ) : (
          timeOff.map((entry) => (
            <div
              key={entry.id}
              className="flex flex-wrap items-center justify-between gap-4 py-4"
            >
              <div>
                <h3 className="font-medium">
                  {entry.timeOffDate}
                </h3>

                <p className="mt-1 text-sm text-neutral-500">
                  {entry.fullDay
                    ? "Full day"
                    : `${entry.startTime?.slice(0, 5)} – ${entry.endTime?.slice(0, 5)}`}
                </p>

                {entry.reason && (
                  <p className="mt-1 text-sm text-neutral-500">
                    {entry.reason}
                  </p>
                )}
              </div>

              <div className="flex gap-2">
                <button
                  type="button"
                  onClick={() => beginEdit(entry)}
                  disabled={saving || deletingId !== null}
                  className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50 disabled:opacity-50"
                >
                  Edit
                </button>

                <button
                  type="button"
                  onClick={() => handleDelete(entry.id)}
                  disabled={saving || deletingId !== null}
                  className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50 disabled:opacity-50"
                >
                  {deletingId === entry.id
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

export default function TimeOffManager({ employees }: Props) {
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
          Employee Time Off
        </h2>

        <p className="mt-1 text-neutral-500">
          Record full-day and partial-day employee absences.
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
            <EmployeeTimeOffEditor
              key={employeeId}
              employeeId={employeeId}
            />
          )}
        </>
      )}
    </section>
  );
}
