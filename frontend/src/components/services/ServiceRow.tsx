"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";

import {
  deactivateService,
  type SalonService,
  type ServiceCategory,
  updateService,
} from "@/lib/api/serviceCatalog";

import EditServiceForm from "./EditServiceForm";

type Props = {
  service: SalonService;
  categories: ServiceCategory[];
};

export default function ServiceRow({
  service,
  categories,
}: Props) {
  const router = useRouter();

  const [editing, setEditing] = useState(false);
  const [changingStatus, setChangingStatus] = useState(false);
  const [statusError, setStatusError] = useState("");

  async function handleStatusChange() {
    setChangingStatus(true);
    setStatusError("");

    try {
      if (service.active) {
        await deactivateService(service.id);
      } else {
        await updateService(service.id, {
          active: true,
        });
      }

      router.refresh();
    } catch (error) {
      console.error(error);

      setStatusError(
        error instanceof Error
          ? error.message
          : "Could not update service status.",
      );
    } finally {
      setChangingStatus(false);
    }
  }

  if (editing) {
    return (
      <div className="py-4">
        <div className="flex items-center justify-between">
          <h3 className="font-medium">
            Editing {service.name}
          </h3>
        </div>

        <EditServiceForm
          service={service}
          categories={categories}
          onCancel={() => setEditing(false)}
        />
      </div>
    );
  }

  return (
    <div className="py-4">
      <div className="flex items-center justify-between gap-6">
        <div>
          <div className="flex items-center gap-3">
            <h3 className="font-medium">
              {service.name}
            </h3>

            {!service.active && (
              <span className="rounded-full bg-neutral-100 px-2 py-0.5 text-xs text-neutral-600">
                Inactive
              </span>
            )}
          </div>

          {service.description && (
            <p className="mt-1 text-sm text-neutral-500">
              {service.description}
            </p>
          )}

          <p className="mt-2 text-sm text-neutral-500">
            {service.durationMinutes} min
            {service.bufferMinutes > 0 &&
              ` + ${service.bufferMinutes} min buffer`}
          </p>
        </div>

        <div className="flex items-center gap-5">
          <div className="text-right">
            <p className="text-lg font-semibold">
              ${Number(service.price).toFixed(2)}
            </p>
          </div>

          <div className="flex gap-2">
            <button
              type="button"
              onClick={() => setEditing(true)}
              className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50"
            >
              Edit
            </button>

            <button
              type="button"
              onClick={handleStatusChange}
              disabled={changingStatus}
              className="rounded-lg border border-neutral-300 px-3 py-2 text-sm font-medium hover:bg-neutral-50 disabled:opacity-50"
            >
              {changingStatus
                ? "Updating..."
                : service.active
                  ? "Deactivate"
                  : "Reactivate"}
            </button>
          </div>
        </div>
      </div>

      {statusError && (
        <p className="mt-2 text-sm text-red-600">{statusError}</p>
      )}
    </div>
  );
}
