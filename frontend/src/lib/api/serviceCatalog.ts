import ServicesPage from "@/app/services/page";

export type ServiceCategory = {
  id: number;
  name: string;
  description: string | null;
  displayOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type SalonService = {
  id: number;
  categoryId: number;
  categoryName: string;
  name: string;
  description: string | null;
  price: number;
  durationMinutes: number;
  bufferMinutes: number;
  displayOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
};

export type UpdateServiceRequest = {
  categoryId?: number;
  name?: string;
  description?: string;
  price?: number;
  durationMinutes?: number;
  bufferMinutes?: number;
  displayOrder?: number;
  active?: boolean;
};

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8080/api";

async function throwApiError(
  response: Response,
  fallbackMessage: string,
): Promise<never> {
  try {
    const body = await response.json();

    throw new Error(
      body.message ?? fallbackMessage,
    );
  } catch (error) {
    if (error instanceof Error) {
      throw error;
    }

    throw new Error(fallbackMessage);
  }
}

export async function getServiceCategories(): Promise<ServiceCategory[]> {
  const response = await fetch(
    `${API_BASE_URL}/service-categories`,
    {
      cache: "no-store",
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to load service categories",
    );
  }

  return response.json();
}

export async function getServices(): Promise<SalonService[]> {
  const response = await fetch(
    `${API_BASE_URL}/services`,
    {
      cache: "no-store",
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to load services",
    );
  }

  return response.json();
}

export type CreateCategoryRequest = {
  name: string;
  description?: string;
  displayOrder?: number;
};

export type CreateServiceRequest = {
  categoryId: number;
  name: string;
  description?: string;
  price: number;
  durationMinutes: number;
  bufferMinutes?: number;
  displayOrder?: number;
};

export async function createServiceCategory(
  request: CreateCategoryRequest,
): Promise<ServiceCategory> {
  const response = await fetch(
    `${API_BASE_URL}/service-categories`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to create category",
    );
  }

  return response.json();
}

export async function createService(
  request: CreateServiceRequest,
): Promise<SalonService> {
  const response = await fetch(
    `${API_BASE_URL}/services`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to create service",
    );
  }

  return response.json();
}

export async function updateService(
  serviceId: number,
  request: UpdateServiceRequest,
): Promise<SalonService> {
  const response = await fetch(
    `${API_BASE_URL}/services/${serviceId}`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to update service",
    );
  }

  return response.json();
}

export async function deactivateService(
  serviceId: number,
): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/services/${serviceId}`,
    {
      method: "DELETE",
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to deactivate service",
    );
  }
}

