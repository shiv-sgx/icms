@capability:document-upload
Feature: Claim document upload
  In order to provide evidence for my claim
  As a customer or surveyor
  I want to upload supporting documents so they can be verified

  # DocumentService.upload validates:
  #   - file must be present (not null)
  #   - docType must not be blank
  #   - extension must be in: pdf, jpg, jpeg, png, gif, doc, docx
  #   - maximum multipart size: 10 MB (struts.multipart.maxSize)
  # Customer uploads via /uploadDocument; surveyor uploads via /uploadReport.

  Background:
    Given the user "customer" with password "Password@123" is logged in

  @critical @smoke
  Scenario: Customer uploads a valid PDF document to their claim
    Given claim id 1 belongs to the logged-in customer
    When the customer uploads file "policy.pdf" of type "Policy Document" to claim 1
    Then the upload succeeds and the customer is redirected back to the claim

  @major @validation
  Scenario Outline: Upload is rejected for disallowed file extensions
    Given claim id 1 belongs to the logged-in customer
    When the customer uploads file "<filename>" of type "Policy Document" to claim 1
    Then the upload is rejected with a message containing "Unsupported file type"

    Examples:
      | filename         |
      | malware.exe      |
      | script.php       |
      | archive.zip      |
      | spreadsheet.xls  |

  @major @validation
  Scenario Outline: Upload succeeds for every allowed file extension
    Given claim id 1 belongs to the logged-in customer
    When the customer uploads file "doc.<ext>" of type "Evidence" to claim 1
    Then the upload succeeds and the customer is redirected back to the claim

    Examples:
      | ext   |
      | pdf   |
      | jpg   |
      | jpeg  |
      | png   |
      | gif   |
      | doc   |
      | docx  |

  @major @validation
  Scenario: Upload is rejected when no file is chosen
    Given claim id 1 belongs to the logged-in customer
    When the customer submits an upload request with no file to claim 1
    Then the upload is rejected with a message containing "Please choose a file"

  @major @validation
  Scenario: Upload is rejected when document type is blank
    Given claim id 1 belongs to the logged-in customer
    When the customer uploads file "photo.jpg" with blank document type to claim 1
    Then the upload is rejected with a message containing "Please specify the document type"

  @major @security
  Scenario: Customer cannot upload a document to a claim they do not own
    Given claim id 2 does not belong to the logged-in customer
    When the customer attempts to upload file "photo.jpg" of type "Evidence" to claim 2
    Then the upload is rejected or the customer is redirected away
