"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

import { createEmployee } from "@/lib/api/employees";

export default function CreateEmployeeForm() {
  const router = useRouter();

  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [phone, setPhone] = useState("");
  const [email, setEmail] = useState("");

  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    setSaving(true);
    setMessage("");

    try {
      await createEmployee({
        firstName,
        lastName,
        displayName,
        phone,
        email,
      });

      setFirstName("");
      setLastName("");
      setDisplayName("");
      setPhone("");
      setEmail("");

      setMessage("Employee created.");

      router.refresh();
    } catch (error) {
      setMessage(
        error instanceof Error
          ? error.message
          : "Failed to create employee",
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="rounded-xl border border-neutral-200 bg-white p-6"
    >
      <h2 className="text-xl font-semibold">Add employee</h2>
      <p className="mt-1 text-sm text-neutral-500">
        Add a team member and their contact information.
      </p>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
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
      </div>

      <button
        type="submit"
        disabled={saving}
        className="mt-6 rounded-lg bg-neutral-900 px-4 py-2 text-white disabled:opacity-50"
      >
        {saving ? "Adding..." : "Add employee"}
      </button>

      {message && (
        <p role="status" className="mt-3 text-sm text-neutral-600">
          {message}
        </p>
      )}
    </form>
  );
}
