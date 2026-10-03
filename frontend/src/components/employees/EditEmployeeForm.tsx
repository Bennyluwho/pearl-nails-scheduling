"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

import {
  Employee,
  updateEmployee,
} from "@/lib/api/employees";

interface EditEmployeeFormProps {
  employee: Employee;
  onCancel: () => void;
}

export default function EditEmployeeForm({
  employee,
  onCancel,
}: EditEmployeeFormProps) {
  const router = useRouter();

  const [firstName, setFirstName] =
    useState(employee.firstName);

  const [lastName, setLastName] =
    useState(employee.lastName);

  const [displayName, setDisplayName] =
    useState(employee.displayName ?? "");

  const [phone, setPhone] =
    useState(employee.phone ?? "");

  const [email, setEmail] =
    useState(employee.email ?? "");

  const [displayOrder, setDisplayOrder] =
    useState(employee.displayOrder.toString());

  const [saving, setSaving] =
    useState(false);

  const [errorMessage, setErrorMessage] =
    useState("");

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    setSaving(true);
    setErrorMessage("");

    try {
      await updateEmployee(
        employee.id,
        {
          firstName,
          lastName,
          displayName,
          phone,
          email,
          displayOrder:
            Number(displayOrder),
        },
      );

      router.refresh();
      onCancel();
    } catch (error) {
      setErrorMessage(
        error instanceof Error
          ? error.message
          : "Failed to update employee",
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="mt-4 rounded-lg border border-neutral-200 bg-neutral-50 p-4"
    >
      <div className="grid gap-4 sm:grid-cols-2">
        <label className="grid min-w-0 gap-2">
          <span className="text-sm font-medium">First name</span>
          <input
            type="text"
            value={firstName}
            onChange={(event) => setFirstName(event.target.value)}
            autoComplete="given-name"
            required
            className="min-w-0 rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>

        <label className="grid min-w-0 gap-2">
          <span className="text-sm font-medium">Last name</span>
          <input
            type="text"
            value={lastName}
            onChange={(event) => setLastName(event.target.value)}
            autoComplete="family-name"
            required
            className="min-w-0 rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>

        <label className="grid min-w-0 gap-2">
          <span className="text-sm font-medium">Display name</span>
          <input
            type="text"
            value={displayName}
            onChange={(event) => setDisplayName(event.target.value)}
            autoComplete="nickname"
            className="min-w-0 rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>

        <label className="grid min-w-0 gap-2">
          <span className="text-sm font-medium">Phone</span>
          <input
            type="tel"
            value={phone}
            onChange={(event) => setPhone(event.target.value)}
            autoComplete="tel"
            className="min-w-0 rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>

        <label className="grid min-w-0 gap-2">
          <span className="text-sm font-medium">Email</span>
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
            className="min-w-0 rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>

        <label className="grid min-w-0 gap-2">
          <span className="text-sm font-medium">Display order</span>
          <input
            type="number"
            value={displayOrder}
            onChange={(event) => setDisplayOrder(event.target.value)}
            min="0"
            className="min-w-0 rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>
      </div>

      <div className="mt-4 flex flex-wrap gap-3">
        <button
          type="submit"
          disabled={saving}
          className="rounded-lg bg-neutral-900 px-4 py-2 text-white disabled:opacity-50"
        >
          {saving ? "Saving..." : "Save changes"}
        </button>

        <button
          type="button"
          onClick={onCancel}
          disabled={saving}
          className="rounded-lg border border-neutral-300 bg-white px-4 py-2 disabled:opacity-50"
        >
          Cancel
        </button>
      </div>

      {errorMessage && (
        <p role="alert" className="mt-3 text-sm text-red-600">
          {errorMessage}
        </p>
      )}
    </form>
  );
}
