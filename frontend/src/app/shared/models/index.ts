/** Shared domain & transport types. DB enums are modeled as string unions. */

export type Role = 'CUSTOMER' | 'AGENT' | 'SURVEYOR' | 'MANAGER' | 'ADMIN';

export interface AuthUser {
  id: number;
  username: string;
  fullName: string;
  email?: string;
  role: Role;
  branch?: string | null;
}

export interface LoginResponse {
  token: string;
  user: AuthUser;
}

/** Standard success envelope returned by the API. */
export interface ApiEnvelope<T> {
  data: T;
  correlationId: string;
}

/** Standard error envelope returned by the API. */
export interface ApiError {
  error: { message: string; fields?: Record<string, string> };
  correlationId: string;
}

/** Paginated list payload. */
export interface Paged<T> {
  items: T[];
  page: number;
  size: number;
  total: number;
}

export interface FaqItem {
  q: string;
  a: string;
}

/** A claim as projected to the customer (statusLabel/statusPill computed server-side). */
export interface Claim {
  id: number;
  claimNo: string;
  policyId: number;
  policyholderId: number;
  claimantName: string;
  claimType: string;
  claimSubtype?: string | null;
  incidentDate?: string | null;
  incidentTime?: string | null;
  incidentLocation?: string | null;
  city?: string | null;
  state?: string | null;
  pinCode?: string | null;
  description?: string | null;
  estimatedLoss: string;
  vehicleRegNo?: string | null;
  firNumber?: string | null;
  policeStation?: string | null;
  hospitalName?: string | null;
  workshopName?: string | null;
  thirdParty?: string | null;
  status: string;
  statusLabel: string;
  statusPill: string;
  policyNo?: string | null;
  agentName?: string | null;
  filedAt?: string | null;
  withdrawable?: boolean;
  // agent/manager-only fields (present in later phases)
  riskLevel?: string;
  fraudScore?: number;
  internalNotes?: string | null;
  surveyorName?: string | null;
}

export interface Policy {
  id: number;
  policyNo: string;
  policyholderId: number;
  type: string;
  sumInsured: string;
  premium: string;
  startDate?: string | null;
  expiryDate?: string | null;
  ncbDiscount: string;
  status: string;
  displayLabel?: string;
}

export interface Policyholder {
  id: number;
  firstName: string;
  lastName: string;
  fullName: string;
  dob?: string | null;
  email?: string;
  mobile?: string;
  address?: string;
  city?: string;
  state?: string;
  pinCode?: string;
}

export interface ClaimDocument {
  id: number;
  claimId: number;
  docType: string;
  fileName?: string | null;
  filePath?: string | null;
  uploadStatus: string;
  verificationStatus: string;
  uploadedAt?: string | null;
}

export interface Communication {
  id: number;
  claimId: number;
  senderId?: number | null;
  senderName?: string | null;
  channel: string;
  content: string;
  createdAt?: string | null;
  claimNo?: string | null;
}

export interface Notification {
  id: number;
  userId?: number | null;
  targetRole?: string | null;
  type: string;
  message: string;
  isRead: boolean | number;
  createdAt?: string | null;
}

export interface TimelineStage {
  label: string;
  state: 'done' | 'current' | 'pending';
}

export interface ClaimBundle {
  claim: Claim;
  timeline: TimelineStage[];
  documents: ClaimDocument[];
  messages: Communication[];
}

export interface CustomerDashboard {
  hasProfile: boolean;
  totalClaims: number;
  openClaims: number;
  settledClaims: number;
  recentClaims: Claim[];
  notifications: Notification[];
}
