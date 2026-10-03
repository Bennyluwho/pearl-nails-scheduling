"use client";

import { useEffect, useMemo, useState } from "react";

import {
  assignEmployeeService,
  getEmployeeServices,
  removeEmployeeService,
} from "@/lib/api/employees";

import type { SalonService } from "@/lib/api/serviceCatalog";

interface EmployeeServicesManagerProps {
  employeeId: number;
  employeeActive: boolean;
  services: SalonService[];
}

export default function EmployeeServicesManager({
  employeeId,
  employeeActive,
  services,
}: EmployeeServicesManagerProps) {
  const [assignedIds, setAssignedIds] =
    useState<Set<number>>(new Set());

  const [loading, setLoading] = useState(true);
  const [changingServiceId, setChangingServiceId] =
    useState<number | null>(null);

  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    async function loadAssignments() {
      setLoading(true);
      setErrorMessage("");

      try {
        const assignedServices =
          await getEmployeeServices(employeeId);

        setAssignedIds(
          new Set(
            assignedServices.map(
              (service) => service.id,
            ),
          ),
        );
      } catch (error) {
        setErrorMessage(
          error instanceof Error
            ? error.message
            : "Failed to load employee services",
        );
      } finally {
        setLoading(false);
      }
    }

    loadAssignments();
  }, [employeeId]);

  const groupedServices = useMemo(() => {
    const groups = new Map<
      string,
      SalonService[]
    >();

    for (const service of services) {
      const existing =
        groups.get(service.categoryName) ?? [];

      existing.push(service);

      groups.set(
        service.categoryName,
        existing,
      );
    }

    return groups;
  }, [services]);

  async function handleToggle(
    service: SalonService,
  ) {
    const currentlyAssigned =
      assignedIds.has(service.id);

    setChangingServiceId(service.id);
    setErrorMessage("");

    try {
      if (currentlyAssigned) {
        await removeEmployeeService(
          employeeId,
          service.id,
        );

        setAssignedIds((current) => {
          const updated = new Set(current);
          updated.delete(service.id);

          return updated;
        });
      } else {
        await assignEmployeeService(
          employeeId,
          service.id,
        );

        setAssignedIds((current) => {
          const updated = new Set(current);
          updated.add(service.id);

          return updated;
        });
      }
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to update service assignment",
      );
    } finally {
      setChangingServiceId(null);
    }
  }

  if (loading) {
    return (
      <p className="mt-4 text-sm text-gray-500">
        Loading service assignments...
      </p>
    );
  }

  return (
    <div className="mt-5 border-t pt-5">
      <div className="mb-4">
        <h3 className="font-semibold">
          Services
        </h3>

        <p className="mt-1 text-sm text-gray-500">
          Select the services this employee can
          perform.
        </p>

        {!employeeActive && (
          <p className="mt-2 text-sm text-amber-600">
            This employee is inactive. Existing
            assignments can be removed, but new
            services cannot be assigned.
          </p>
        )}
      </div>

      {errorMessage && (
        <p className="mb-4 text-sm text-red-600">
          {errorMessage}
        </p>
      )}

      <div className="space-y-6">
        {Array.from(groupedServices.entries()).map(
          ([categoryName, categoryServices]) => (
            <div key={categoryName}>
              <h4 className="mb-2 font-medium">
                {categoryName}
              </h4>

              <div className="space-y-2">
                {categoryServices.map((service) => {
                  const assigned =
                    assignedIds.has(service.id);

                  const changing =
                    changingServiceId === service.id;

                  const cannotAdd =
                    !assigned &&
                    (!employeeActive ||
                      !service.active);

                  return (
                    <label
                      key={service.id}
                      className="flex items-center justify-between gap-4 rounded border p-3"
                    >
                      <div className="flex items-center gap-3">
                        <input
                          type="checkbox"
                          checked={assigned}
                          disabled={
                            changing || cannotAdd
                          }
                          onChange={() =>
                            handleToggle(service)
                          }
                        />

                        <div>
                          <p className="font-medium">
                            {service.name}
                          </p>

                          <p className="text-sm text-gray-500">
                            {service.durationMinutes} min
                            {" · "}
                            ${Number(
                              service.price,
                            ).toFixed(2)}
                          </p>
                        </div>
                      </div>

                      {!service.active && (
                        <span className="text-xs text-gray-500">
                          Inactive
                        </span>
                      )}

                      {changing && (
                        <span className="text-xs text-gray-500">
                          Updating...
                        </span>
                      )}
                    </label>
                  );
                })}
              </div>
            </div>
          ),
        )}
      </div>
    </div>
  );
}