export interface BusinessHours {
    id: number;
    dayOfWeek: number;
    openTime: string | null;
    closeTime: string | null;
    closed: boolean;
}

export interface UpdateBusinessHoursRequest {
    closed: boolean;
    openTime: string | null;
    closeTime: string | null;
}

const API_BASE_URL = 
    process.env.NEXT_PUBLIC_API_BASE_URL ??
    "http://localhost:8080/api";

async function throwApiError(
    response: Response,
    fallbackMessage: string,
): Promise<never> {
    let message = fallbackMessage;

    try {
        const body = await response.json();
        message = body.message ?? fallbackMessage;
    } catch {
        // Keep the fallback when the response has no JSON body.
    }

    throw new Error(message);
}

export async function getBusinessHours(): Promise<BusinessHours[]> {
  const response = await fetch(
    `${API_BASE_URL}/business-hours`,
    { cache: "no-store" },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to load business hours",
    );
  }

  return response.json();
}

export async function updateBusinessHours(
    dayOfWeek: number,
    request: UpdateBusinessHoursRequest,
): Promise<BusinessHours> {
    const response = await fetch(
        `${API_BASE_URL}/business-hours/${dayOfWeek}`,
        {
            method: "PUT",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(request),
        },
    );

    if (!response.ok) {
        return throwApiError(
            response,
            "Failed to update business hours",
        );
    }

    return response.json();
}


export interface EmployeeWorkingHours {
  id: number;
  employeeId: number;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
}

export interface CreateWorkingHoursRequest {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
}

export type UpdateWorkingHoursRequest =
  Partial<CreateWorkingHoursRequest>;

export async function getEmployeeWorkingHours(
  employeeId: number,
): Promise<EmployeeWorkingHours[]> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${employeeId}/working-hours`,
    { cache: "no-store" },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to load employee working hours",
    );
  }

  return response.json();
}

export async function createEmployeeWorkingHours(
  employeeId: number,
  request: CreateWorkingHoursRequest,
): Promise<EmployeeWorkingHours> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${employeeId}/working-hours`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to create employee shift",
    );
  }

  return response.json();
}

export async function updateEmployeeWorkingHours(
  employeeId: number,
  shiftId: number,
  request: UpdateWorkingHoursRequest,
): Promise<EmployeeWorkingHours> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${employeeId}/working-hours/${shiftId}`,
    {
      method: "PATCH",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(request),
    },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to update employee shift",
    );
  }

  return response.json();
}

export async function deleteEmployeeWorkingHours(
  employeeId: number,
  shiftId: number,
): Promise<void> {
  const response = await fetch(
    `${API_BASE_URL}/employees/${employeeId}/working-hours/${shiftId}`,
    { method: "DELETE" },
  );

  if (!response.ok) {
    return throwApiError(
      response,
      "Failed to delete employee shift",
    );
  }
}