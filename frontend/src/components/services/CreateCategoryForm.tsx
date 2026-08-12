"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

import { createServiceCategory } from "@/lib/api/serviceCatalog";

export default function CreateCategoryForm() {
  const router = useRouter();

  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [displayOrder, setDisplayOrder] = useState("");

  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    setSaving(true);
    setMessage("");

    try {
      await createServiceCategory({
        name,
        description:
          description.trim() === ""
            ? undefined
            : description,
        displayOrder:
          displayOrder === ""
            ? 0
            : Number(displayOrder),
      });

      setName("");
      setDescription("");
      setDisplayOrder("");

      setMessage("Category created.");

      router.refresh();
    } catch (error) {
      console.error(error);

      setMessage(
        error instanceof Error
          ? error.message
          : "Could not create category.",
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
      <h2 className="text-xl font-semibold">
        Add category
      </h2>

      <p className="mt-1 text-sm text-neutral-500">
        Create a group for related salon services.
      </p>

      <div className="mt-6 grid gap-4 sm:grid-cols-2">

        <label className="grid gap-2">
          <span className="text-sm font-medium">
            Category name
          </span>

          <input
            value={name}
            onChange={(event) =>
              setName(event.target.value)
            }
            required
            maxLength={100}
            placeholder="e.g. Manicures"
            className="rounded-lg border border-neutral-300 p-2"
          />
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium">
            Display order
          </span>

          <input
            type="number"
            min="0"
            value={displayOrder}
            onChange={(event) =>
              setDisplayOrder(event.target.value)
            }
            placeholder="0"
            className="rounded-lg border border-neutral-300 p-2"
          />
        </label>

        <label className="grid gap-2 sm:col-span-2">
          <span className="text-sm font-medium">
            Description
          </span>

          <textarea
            value={description}
            onChange={(event) =>
              setDescription(event.target.value)
            }
            maxLength={500}
            rows={3}
            placeholder="Optional description"
            className="rounded-lg border border-neutral-300 p-2"
          />
        </label>

      </div>

      <button
        type="submit"
        disabled={saving}
        className="mt-6 rounded-lg bg-neutral-900 px-4 py-2 text-white disabled:opacity-50"
      >
        {saving ? "Saving..." : "Add category"}
      </button>

      {message && (
        <p className="mt-3 text-sm text-neutral-600">
          {message}
        </p>
      )}
    </form>
  );
}