
"use client";

import { useState, type FormEvent } from "react";
import { useRouter } from "next/navigation";

import {
  type BusinessHours,
  updateBusinessHours,
} from "@/lib/api/scheduling";

interface Props {
  hours: BusinessHours[];
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

function BusinessHoursRow({
  hours,
}: {
  hours: BusinessHours;
}) {
  const router = useRouter();

  const [editing, setEditing] = useState(false);
  const [closed, setClosed] = useState(hours.closed);
  const [openTime, setOpenTime] = useState(
    hours.openTime?.slice(0, 5) ?? "09:00",
  );
  const [closeTime, setCloseTime] = useState(
    hours.closeTime?.slice(0, 5) ?? "19:00",
  );

  const [saving, setSaving] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  function handleCancel() {
    setClosed(hours.closed);
    setOpenTime(hours.openTime?.slice(0, 5) ?? "09:00");
    setCloseTime(hours.closeTime?.slice(0, 5) ?? "19:00");
    setErrorMessage("");
    setEditing(false);
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    setSaving(true);
    setErrorMessage("");

    try {
      await updateBusinessHours(hours.dayOfWeek, {
        closed,
        openTime: closed ? null : openTime,
        closeTime: closed ? null : closeTime,
      });

      setEditing(false);
      router.refresh();
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to update hours",
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="py-4">
      {!editing ? (
        <div className="flex flex-wrap items-center justify-between gap-4">
          <div>
            <h3 className="font-medium">
              {DAYS[hours.dayOfWeek]}
            </h3>

            <p className="mt-1 text-sm text-neutral-500">
              {hours.closed
                ? "Closed"
                : `${hours.openTime?.slice(0, 5)} – ${hours.closeTime?.slice(0, 5)}`}
            </p>
          </div>

          <button
            type="button"
            onClick={() => setEditing(true)}
            className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50"
          >
            Edit
          </button>
        </div>
      ) : (
        <form onSubmit={handleSubmit}>
          <h3 className="mb-4 font-medium">
            Edit {DAYS[hours.dayOfWeek]}
          </h3>

          <div className="grid gap-4 sm:grid-cols-3">
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={closed}
                onChange={(event) =>
                  setClosed(event.target.checked)
                }
              />
              <span className="text-sm font-medium">
                Closed
              </span>
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium">
                Opening time
              </span>
              <input
                type="time"
                value={openTime}
                onChange={(event) =>
                  setOpenTime(event.target.value)
                }
                disabled={closed}
                required={!closed}
                className="rounded-lg border border-neutral-300 bg-white p-2"
              />
            </label>

            <label className="grid gap-2">
              <span className="text-sm font-medium">
                Closing time
              </span>
              <input
                type="time"
                value={closeTime}
                onChange={(event) =>
                  setCloseTime(event.target.value)
                }
                disabled={closed}
                required={!closed}
                className="rounded-lg border border-neutral-300 bg-white p-2"
              />
            </label>
          </div>

          <div className="mt-4 flex gap-3">
            <button
              type="submit"
              disabled={saving}
              className="rounded-lg bg-neutral-900 px-4 py-2 text-white disabled:opacity-50"
            >
              {saving ? "Saving..." : "Save changes"}
            </button>

            <button
              type="button"
              onClick={handleCancel}
              disabled={saving}
              className="rounded-lg border border-neutral-300 bg-white px-4 py-2"
            >
              Cancel
            </button>
          </div>

          {errorMessage && (
            <p
              role="alert"
              className="mt-3 text-sm text-red-600"
            >
              {errorMessage}
            </p>
          )}
        </form>
      )}
    </div>
  );
}

export default function BusinessHoursManager({
  hours,
}: Props) {
  return (
    <section className="rounded-xl border border-neutral-200 bg-white p-6 shadow-sm">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold">
          Business Hours
        </h2>

        <p className="mt-1 text-neutral-500">
          Set the salon&apos;s weekly opening hours.
        </p>
      </div>

      <div className="divide-y divide-neutral-200">
        {hours.map((day) => (
          <BusinessHoursRow
            key={day.id}
            hours={day}
          />
        ))}
      </div>
    </section>
  );
}
