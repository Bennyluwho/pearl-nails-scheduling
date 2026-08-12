import CreateServiceForm from "@/components/services/CreateServiceForm";
import CreateCategoryForm from "@/components/services/CreateCategoryForm";
import ServiceRow from "@/components/services/ServiceRow";

import {
  getServiceCategories,
  getServices,
} from "@/lib/api/serviceCatalog";

export default async function ServicesPage() {
  const [categories, services] = await Promise.all([
    getServiceCategories(),
    getServices(),
  ]);

  return (
    <main className="min-h-screen bg-neutral-50 p-8 text-neutral-900">
      <div className="mx-auto max-w-6xl">

        <header className="mb-10">
          <p className="text-sm font-medium uppercase tracking-widest text-neutral-500">
            Pearl Nails
          </p>

          <h1 className="mt-2 text-4xl font-semibold tracking-tight">
            Services
          </h1>

          <p className="mt-3 text-neutral-600">
            Manage service categories, prices, durations, and availability.
          </p>
        </header>

        <div className="mb-10 grid gap-6 lg:grid-cols-2">
          <CreateCategoryForm />

          <CreateServiceForm categories={categories} />
        </div>

        <div className="space-y-8">
          {categories.map((category) => {
            const categoryServices = services.filter(
              (service) =>
                service.categoryId === category.id,
            );

            return (
              <section
                key={category.id}
                className="rounded-xl border border-neutral-200 bg-white p-6 shadow-sm"
              >
                <div className="mb-6 flex items-start justify-between">
                  <div>
                    <h2 className="text-2xl font-semibold">
                      {category.name}
                    </h2>

                    {category.description && (
                      <p className="mt-1 text-neutral-500">
                        {category.description}
                      </p>
                    )}
                  </div>

                  {!category.active && (
                    <span className="rounded-full bg-neutral-100 px-3 py-1 text-sm text-neutral-600">
                      Inactive
                    </span>
                  )}
                </div>

                {categoryServices.length === 0 ? (
                  <p className="text-neutral-500">
                    No services in this category.
                  </p>
                ) : (
                  <div className="divide-y divide-neutral-200">
                    {categoryServices.map((service) => (
                      <ServiceRow
                        key={service.id}
                        service={service}
                        categories={categories}
                      />
                    ))}
                  </div>
                )}
              </section>
            );
          })}
        </div>
      </div>
    </main>
  );
}