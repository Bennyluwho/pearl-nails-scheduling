"use client";

import { FormEvent, useState } from "react";
import { useRouter } from "next/navigation";

import {
  SalonService,
  ServiceCategory,
  updateService,
} from "@/lib/api/serviceCatalog";

type Props = {
  service: SalonService;
  categories: ServiceCategory[];
  onCancel: () => void;
};

export default function EditServiceForm({
  service,
  categories,
  onCancel,
}: Props) {
  const router = useRouter();

  const [changingStatus, setChangingStatus] =
  useState(false);

  const [statusError, setStatusError] =
  useState("");

  const [categoryId, setCategoryId] = useState(
    String(service.categoryId),
  );

  const [name, setName] = useState(service.name);

  const [description, setDescription] = useState(
    service.description ?? "",
  );

  const [price, setPrice] = useState(
    String(service.price),
  );

  const [durationMinutes, setDurationMinutes] = useState(
    String(service.durationMinutes),
  );

  const [bufferMinutes, setBufferMinutes] = useState(
    String(service.bufferMinutes),
  );

  const [displayOrder, setDisplayOrder] = useState(
    String(service.displayOrder),
  );

  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");


  
  

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault();

    setSaving(true);
    setMessage("");

    try {
      await updateService(service.id, {
        categoryId: Number(categoryId),
        name,
        description:
          description.trim() === ""
            ? ""
            : description,
        price: Number(price),
        durationMinutes: Number(durationMinutes),
        bufferMinutes: Number(bufferMinutes),
        displayOrder: Number(displayOrder),
      });

      setMessage("Service updated.");

      router.refresh();
      onCancel();
    } catch (error) {
      setMessage(
        error instanceof Error
        ? error.message
        : "Could not update service.",
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

        <label className="grid gap-2">
          <span className="text-sm font-medium">
            Category
          </span>

          <select
            value={categoryId}
            onChange={(event) =>
              setCategoryId(event.target.value)
            }
            required
            className="rounded-lg border border-neutral-300 bg-white p-2"
          >
            {categories
              .filter(
                (category) =>
                  category.active ||
                  category.id === service.categoryId,
              )
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
          <span className="text-sm font-medium">
            Service name
          </span>

          <input
            value={name}
            onChange={(event) =>
              setName(event.target.value)
            }
            required
            maxLength={150}
            className="rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium">
            Price
          </span>

          <input
            type="number"
            min="0"
            step="0.01"
            value={price}
            onChange={(event) =>
              setPrice(event.target.value)
            }
            required
            className="rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium">
            Duration
          </span>

          <input
            type="number"
            min="1"
            value={durationMinutes}
            onChange={(event) =>
              setDurationMinutes(event.target.value)
            }
            required
            className="rounded-lg border border-neutral-300 bg-white p-2"
          />
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium">
            Buffer
          </span>

          <input
            type="number"
            min="0"
            value={bufferMinutes}
            onChange={(event) =>
              setBufferMinutes(event.target.value)
            }
            required
            className="rounded-lg border border-neutral-300 bg-white p-2"
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
            required
            className="rounded-lg border border-neutral-300 bg-white p-2"
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
            rows={3}
            maxLength={1000}
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
          onClick={onCancel}
          disabled={saving}
          className="rounded-lg border border-neutral-300 bg-white px-4 py-2"
        >
          Cancel
        </button>
      </div>

      {message && (
        <p className="mt-3 text-sm text-neutral-600">
          {message}
        </p>
      )}
    </form>
  );
}