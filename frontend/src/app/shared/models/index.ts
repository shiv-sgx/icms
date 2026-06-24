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
  agentId?: number | null;
  surveyorId?: number | null;
  updatedAt?: string | null;
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

export interface Approval {
  id: number;
  claimId: number;
  level: string;
  approverId?: number | null;
  approverRole?: string | null;
  decision: string;
  remarks?: string | null;
  decidedAt?: string | null;
  createdAt?: string | null;
  approverName?: string | null;
}

export interface AssessmentComponent {
  id?: number;
  assessmentId?: number;
  component: string;
  severity: string;
  repairCost: string;
  replaceFlag: boolean | number;
}

export interface Assessment {
  id: number;
  claimId: number;
  surveyorId?: number | null;
  visitDate?: string | null;
  visitTime?: string | null;
  siteObservations?: string | null;
  reportRefNo?: string | null;
  grossAssessed: string;
  policyDeductible: string;
  depreciationPct: string;
  depreciationAmt: string;
  salvageValue: string;
  netPayable: string;
  recommendation?: string | null;
  remarks?: string | null;
  status: string;
  createdAt?: string | null;
  surveyorName?: string | null;
}

export interface Settlement {
  id: number;
  claimId: number;
  finalAmount: string;
  justification?: string | null;
  paymentMethod: string;
  accountHolder?: string | null;
  bankName?: string | null;
  accountNumber?: string | null;
  ifscCode?: string | null;
  status: string;
  approvedBy?: number | null;
}

export interface Surveyor {
  id: number;
  fullName: string;
  branch?: string | null;
}

/** Full agent/manager claim bundle. */
export interface AgentBundle {
  claim: Claim;
  documents: ClaimDocument[];
  messages: Communication[];
  approvals: Approval[];
  settlement: Settlement | null;
  assessment: Assessment | null;
  components: AssessmentComponent[];
  timeline: TimelineStage[];
  surveyors?: Surveyor[];
}

export interface SettlementScreen {
  claim: Claim;
  settlement: Settlement | null;
  tracker: string[];
  suggestedAmount: string;
  paymentMethods: string[];
}

export interface AssessScreen {
  claim: Claim;
  assessment: Assessment | null;
  components: AssessmentComponent[];
  documents: ClaimDocument[];
}

export interface AgentDashboard {
  openClaims: number;
  awaitingSurvey: number;
  pendingApproval: number;
  settled: number;
  worklist: Claim[];
}

export interface SurveyorDashboard {
  totalAssigned: number;
  pendingSurvey: number;
  assessed: number;
  claims: Paged<Claim>;
}

export interface ReportTable {
  key: string;
  title: string;
  headers: string[];
  rows: string[][];
}

export interface ManagerDashboard {
  pendingApproval: number;
  highRisk: number;
  slaBreaches: number;
  settled: number;
  queue: Claim[];
  agentPerformance: ReportTable;
}

export interface AdminStats {
  users: number;
  claims: number;
  roles: number;
  auditEvents: number;
  poolActive: number;
  poolIdle: number;
  poolTotal: number;
}

export interface AdminUser {
  id: number;
  fullName: string;
  email: string;
  username: string;
  roleId: number;
  roleName: string;
  branch?: string | null;
  status: string;
  lastLogin?: string | null;
  createdAt?: string | null;
}

export interface RoleWithCount {
  id: number;
  name: string;
  description?: string | null;
  userCount: number;
}

export interface SlaConfig {
  id: number;
  stage: string;
  hours: number;
}

export interface ApprovalThreshold {
  id: number;
  level: string;
  label: string;
  minAmount: string;
  maxAmount: string | null;
}

export interface NotificationTemplate {
  id: number;
  name: string;
  channel: string;
  active: boolean | number;
  body?: string | null;
}

export interface DocumentRequirement {
  id: number;
  claimType: string;
  claimSubtype?: string | null;
  docType: string;
  required: boolean | number;
}

export interface AuditLog {
  id: number;
  ts?: string | null;
  userId?: number | null;
  username?: string | null;
  role?: string | null;
  action: string;
  entity?: string | null;
  ipAddress?: string | null;
  result: string;
}
