import type { SalonService } from "./serviceCatalog";

export interface Employee {
  id: number;
  firstName: string;
  lastName: string;
  displayName: string | null;
  phone: string | null;
  email: string | null;
  displayOrder: number;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface CreateEmployeeRequest {
  firstName: string;
  lastName: string;
  displayName?: string;
  phone?: string;
  email?: string;
  displayOrder?: number;
}

export interface UpdateEmployeeRequest {
  firstName?: string;
  lastName?: string;
  displayName?: string;
  phone?: string;
  email?: string;
  displayOrder?: number;
  active?: boolean;
}

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
      body.message ?? fallbackMessage
    );
  } catch (error) {
    if (error instanceof Error) {
      throw error;
    }

    throw new Error(fallbackMessage);
  }
}

export async function getEmployees(): Promise<Employee[]> {
  const response = await fetch(
    `${API_BASE_URL}/employees`,
    {
      cache: "no-store",
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to load employees",
    );
  }

  return response.json();
}

export async function createEmployee(
  employee: CreateEmployeeRequest,
): Promise<Employee> {
  const response = await fetch(
    `${API_BASE_URL}/employees`,
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(employee),
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to create employee",
    );
  }

  return response.json();
}

export async function updateEmployee(
  id: number,
  employee: UpdateEmployeeRequest,
): Promise<Employee> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${id}`,
    {
      method: "PATCH",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(employee),
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to update employee",
    );
  }

  return response.json();
}

export async function deactivateEmployee(
  id: number,
): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${id}`,
    {
      method: "DELETE",
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to deactivate employee",
    );
  }
}

export async function getEmployeeServices(
  employeeId: number,
): Promise<SalonService[]> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${employeeId}/services`,
    {
      cache: "no-store",
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to load employee services",
    );
  }

  return response.json();
}

export async function assignEmployeeService(
  employeeId: number,
  serviceId: number,
): Promise<SalonService> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${employeeId}/services/${serviceId}`,
    {
      method: "POST",
    },
  );

  if(!response.ok) {
    return throwApiError(
      response,
      "Failed to assign service"
    );
  }

  return response.json();
} 

export async function removeEmployeeService(
  employeeId: number,
  serviceId: number,
): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${employeeId}/services/${serviceId}`,
    {
      method: "DELETE",
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to remove service",
    );
  }
}