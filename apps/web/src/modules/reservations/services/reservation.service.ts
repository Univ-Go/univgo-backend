import Cookies from "js-cookie";
export interface ReservationData {
  identification: string;
  spaceName: string;
  startTime: string;
  endTime: string;
  reservationDate: string;
  guestsIdentifications?: string[];
}

const API_URL = `${process.env.NEXT_PUBLIC_API_URL}/reservations`;

const getAuthHeaders = () => {
  const token = Cookies.get("token");
  return {
    "Content-Type": "application/json",
    ...(token && { Authorization: `Bearer ${token}` }),
  };
};

export const createReservation = async (reservationData: ReservationData) => {
  const headers = getAuthHeaders();

  const response = await fetch(API_URL, {
    method: "POST",
    headers: headers,
    body: JSON.stringify(reservationData),
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(`Error creating reservation: ${errorData.message}`);
  }

  return response.json();
};

export const getUserReservations = async (userIdentification: string) => {
  const headers = getAuthHeaders();

  const response = await fetch(`${API_URL}/user/${userIdentification}`, {
    method: "GET",
    headers: headers,
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(`Error fetching user reservations: ${errorData.message}`);
  }

  return response.json();
};

export const cancelReservation = async (reservationId: number) => {
  const headers = getAuthHeaders();

  const response = await fetch(`${API_URL}/${reservationId}`, {
    method: "PATCH",
    headers: headers,
    body: JSON.stringify({ status: "cancelled_by_user" }),
  });

  if (!response.ok) {
    const errorData = await response.json();
    throw new Error(`Error cancelling reservation: ${errorData.message}`);
  }
  return response.json();
}
