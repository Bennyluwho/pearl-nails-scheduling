"use client";

import { useRouter } from "next/navigation";
import { useState, type SubmitEvent  } from "react";

import {
  createService,
  type ServiceCategory,
} from "@/lib/api/serviceCatalog";

type Props = {
  categories: ServiceCategory[];
};

export default function CreateServiceForm({
  categories,
}: Props) {
  const router = useRouter();

  const [categoryId, setCategoryId] = useState("");
  const [name, setName] = useState("");
  const [price, setPrice] = useState("");
  const [durationMinutes, setDurationMinutes] = useState("");
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  async function handleSubmit(event: SubmitEvent<HTMLFormElement>) {
    event.preventDefault();

    setSaving(true);
    setMessage("");

    try {
      await createService({
        categoryId: Number(categoryId),
        name,
        price: Number(price),
        durationMinutes: Number(durationMinutes),
        bufferMinutes: 0,
        displayOrder: 0,
      });

      setName("");
      setPrice("");
      setDurationMinutes("");
      setMessage("Service created.");
      router.refresh();
    } catch (error) {
      console.error(error);

      setMessage(
        error instanceof Error
          ? error.message
          : "Could not create service.",
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
      <h2 className="text-xl font-semibold">Add service</h2>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">
        <label className="grid gap-2">
          <span className="text-sm font-medium">Category</span>

          <select
            value={categoryId}
            onChange={(event) => setCategoryId(event.target.value)}
            required
            className="rounded-lg border border-neutral-300 p-2"
          >
            <option value="">Select category</option>

            {categories
              .filter((category) => category.active)
              .map((category) => (
                <option
                  key={category.id}
                  value={category.id}
                >
                  {category.name}
                </option>
              ))}
          </select>
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium">Service name</span>

          <input
            value={name}
            onChange={(event) => setName(event.target.value)}
            required
            className="rounded-lg border border-neutral-300 p-2"
          />
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium">Price</span>

          <input
            type="number"
            min="0"
            step="0.01"
            value={price}
            onChange={(event) => setPrice(event.target.value)}
            required
            className="rounded-lg border border-neutral-300 p-2"
          />
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium">Duration (minutes)</span>

          <input
            type="number"
            min="1"
            value={durationMinutes}
            onChange={(event) => setDurationMinutes(event.target.value)}
            required
            className="rounded-lg border border-neutral-300 p-2"
          />
        </label>
      </div>

      <button
        type="submit"
        disabled={saving}
        className="mt-6 rounded-lg bg-neutral-900 px-4 py-2 text-white disabled:opacity-50"
      >
        {saving ? "Saving..." : "Add service"}
      </button>

      {message && (
        <p className="mt-3 text-sm text-neutral-600">{message}</p>
      )}
    </form>
  );
}
