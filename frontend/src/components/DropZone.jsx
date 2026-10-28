import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, X, AlertCircle, Loader2, CheckCircle2 } from 'lucide-react';
import PdfIcon from './PdfIcon';
import DocumentMockup from './DocumentMockup';
import { uploadResume, runAnalysis } from '../api/client';

/**
 * DropZone component
 * Handles drag and drop file intake, click to browse, client-side validation,
 * metadata and job description form inputs, and the animated scanning state.
 *
 * @param {Object} [props]
 * @param {string} [props.className] - Optional container CSS classes
 */
export default function DropZone({ className = '' }) {
  const navigate = useNavigate();
  const fileInputRef = useRef(null);

  const [file, setFile] = useState(null);
  const [targetRole, setTargetRole] = useState('');
  const [experienceYears, setExperienceYears] = useState('');
  const [jobDescription, setJobDescription] = useState('');
  const [isDragging, setIsDragging] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [currentStep, setCurrentStep] = useState(null); // 'Uploading' | 'Analyzing' | null
  const [errorMessage, setErrorMessage] = useState(null);

  // Client side file validation
  const validateAndSelectFile = (candidateFile) => {
    setErrorMessage(null);
    if (!candidateFile) return;

    const fileName = candidateFile.name.toLowerCase();
    const isPdf =
      fileName.endsWith('.pdf') || candidateFile.type === 'application/pdf';
    const isDocx =
      fileName.endsWith('.docx') ||
      candidateFile.type ===
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document';

    if (!isPdf && !isDocx) {
      setErrorMessage('Unsupported file type. Only PDF and DOCX are supported.');
      return;
    }

    const maxSizeBytes = 10 * 1024 * 1024; // 10 MB
    if (candidateFile.size > maxSizeBytes) {
      setErrorMessage('File too large. Maximum allowed size is 10 MB.');
      return;
    }

    setFile(candidateFile);
  };

  const handleDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(true);
  };

  const handleDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setIsDragging(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      validateAndSelectFile(e.dataTransfer.files[0]);
    }
  };

  const handleFileInputChange = (e) => {
    if (e.target.files && e.target.files.length > 0) {
      validateAndSelectFile(e.target.files[0]);
    }
  };

  const handleRemoveFile = () => {
    setFile(null);
    setErrorMessage(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      fileInputRef.current?.click();
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      setErrorMessage('Please select a resume file to upload.');
      return;
    }

    if (!jobDescription.trim()) {
      setErrorMessage('Job description is required.');
      return;
    }

    setErrorMessage(null);
    setIsSubmitting(true);
    setCurrentStep('Uploading');

    try {
      // Step 1: Upload resume
      const formData = new FormData();
      formData.append('file', file);
      if (targetRole.trim()) {
        formData.append('targetRole', targetRole.trim());
      }
      if (experienceYears !== '' && !isNaN(Number(experienceYears))) {
        formData.append('experienceYears', parseInt(experienceYears, 10));
      }

      const uploadResult = await uploadResume(formData);
      const resumeId = uploadResult?.resumeId;

      if (!resumeId) {
        throw new Error('Upload succeeded but no resume identifier was returned.');
      }

      // Step 2: Run analysis
      setCurrentStep('Analyzing');

      const analysisPayload = {
        resumeId,
        rawJobDescription: jobDescription.trim(),
      };
      if (targetRole.trim()) {
        analysisPayload.jobTitle = targetRole.trim();
      }

      const analysisResult = await runAnalysis(analysisPayload);
      const matchId = analysisResult?.id;

      if (!matchId) {
        throw new Error('Analysis completed but no match identifier was returned.');
      }

      navigate(`/report/${matchId}`);
    } catch (err) {
      setIsSubmitting(false);
      setCurrentStep(null);

      const status = err.status || err.response?.status;
      if (status === 413) {
        setErrorMessage('File too large. Maximum allowed size is 10 MB.');
      } else if (status === 415) {
        setErrorMessage('Unsupported file type. Only PDF and DOCX are supported.');
      } else if (status === 422) {
        setErrorMessage('Unreadable file or no text found. Please check your document.');
      } else if (status === 400) {
        setErrorMessage('Invalid input. Please verify the uploaded file and job description.');
      } else if (status === 404) {
        setErrorMessage('Resource not found. Please try again.');
      } else {
        setErrorMessage(err.message || 'An unexpected error occurred. Please try again.');
      }
    }
  };

  return (
    <div
      className={`rounded-[30px] bg-[var(--bg-card)] border-2 border-dashed transition-all duration-300 p-6 sm:p-8 ${
        isDragging
          ? 'border-[var(--brand-400)] shadow-[var(--shadow-active-glow)]'
          : 'border-[var(--brand-500)] shadow-[var(--shadow-card-hover)]'
      } ${className}`}
    >
      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        onChange={handleFileInputChange}
        className="hidden"
        id="resume-file-input"
        aria-label="Upload resume file"
      />

      {/* Screen reader live updates for loading progression */}
      <div aria-live="polite" className="sr-only">
        {currentStep ? `Processing step: ${currentStep}` : ''}
      </div>

      {/* Error notification banner */}
      {errorMessage && (
        <div
          role="alert"
          className="mb-5 p-3 rounded-[9px] border border-[var(--danger)] bg-[rgba(239,68,68,0.10)] text-[var(--danger)] text-xs flex items-start gap-2.5"
        >
          <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
          <div className="flex-1 leading-relaxed">{errorMessage}</div>
          <button
            type="button"
            onClick={() => setErrorMessage(null)}
            className="text-[var(--danger)] hover:opacity-80 p-0.5"
            aria-label="Dismiss error"
          >
            <X className="w-3.5 h-3.5" />
          </button>
        </div>
      )}

      {/* STATE 1: Analyzing / Scanning progression */}
      {isSubmitting ? (
        <div className="flex flex-col items-center justify-center py-4">
          <div className="w-full max-w-xs mb-6">
            <DocumentMockup scanning={true} />
          </div>

          <div className="w-full max-w-sm bg-[var(--bg-elevated)] border border-[var(--border)] rounded-[12px] p-4 text-center">
            <div className="flex items-center justify-center gap-6 mb-3">
              {/* Step: Uploading */}
              <div className="flex items-center gap-2">
                {currentStep === 'Uploading' ? (
                  <Loader2 className="w-4 h-4 text-[var(--brand-500)] animate-spin" />
                ) : (
                  <CheckCircle2 className="w-4 h-4 text-[var(--success)]" />
                )}
                <span
                  className={`text-xs font-semibold ${
                    currentStep === 'Uploading'
                      ? 'text-[var(--brand-500)]'
                      : 'text-[var(--text-secondary)]'
                  }`}
                >
                  Uploading
                </span>
              </div>

              <span className="text-[var(--border-strong)]">/</span>

              {/* Step: Analyzing */}
              <div className="flex items-center gap-2">
                {currentStep === 'Analyzing' ? (
                  <Loader2 className="w-4 h-4 text-[var(--brand-500)] animate-spin" />
                ) : (
                  <div className="w-3 h-3 rounded-full border-2 border-[var(--border-strong)]" />
                )}
                <span
                  className={`text-xs font-semibold ${
                    currentStep === 'Analyzing'
                      ? 'text-[var(--brand-500)]'
                      : 'text-[var(--text-tertiary)]'
                  }`}
                >
                  Analyzing
                </span>
              </div>
            </div>

            <p className="text-xs text-[var(--text-secondary)]">
              {currentStep === 'Uploading'
                ? 'Extracting resume text and verifying file structure...'
                : 'Scoring six dimensions and computing keyword matches...'}
            </p>
          </div>
        </div>
      ) : !file ? (
        /* STATE 2: Empty intake drop zone */
        <div
          onDragOver={handleDragOver}
          onDragLeave={handleDragLeave}
          onDrop={handleDrop}
          onClick={() => fileInputRef.current?.click()}
          onKeyDown={handleKeyDown}
          tabIndex={0}
          role="button"
          aria-label="Drag and drop resume here or click to browse"
          className="flex flex-col items-center justify-center text-center cursor-pointer py-8 px-4 rounded-[24px] hover:bg-[rgba(255,255,255,0.02)] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
        >
          <div className="mb-4 transform transition-transform duration-200 hover:scale-105">
            <PdfIcon size={56} />
          </div>

          <h3 className="font-heading font-semibold text-base sm:text-lg text-[var(--text)] mb-1">
            Drag and drop your resume here
          </h3>

          <p className="text-sm text-[var(--text-secondary)] mb-4">
            or click to browse
          </p>

          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation();
              fileInputRef.current?.click();
            }}
            className="px-5 py-2 rounded-[9px] text-xs font-semibold bg-[var(--bg-elevated)] hover:bg-[var(--bg-card)] border border-[var(--border)] hover:border-[var(--brand-500)] text-[var(--text)] transition-all duration-150 shadow-sm focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)]"
          >
            Choose file
          </button>

          <span className="text-[11px] font-mono text-[var(--text-tertiary)] mt-4">
            PDF or DOCX, up to 10 MB
          </span>
        </div>
      ) : (
        /* STATE 3: Selected file transformed form */
        <form onSubmit={handleSubmit} className="space-y-4">
          {/* File chip with remove button */}
          <div className="flex items-center justify-between p-3 rounded-[9px] bg-[var(--bg-elevated)] border border-[var(--border)]">
            <div className="flex items-center gap-3 overflow-hidden">
              <div className="w-8 h-8 rounded-[6px] bg-[rgba(232,96,46,0.12)] border border-[rgba(232,96,46,0.25)] flex items-center justify-center text-[var(--brand-500)] shrink-0">
                <FileText className="w-4 h-4" />
              </div>
              <div className="overflow-hidden">
                <p className="text-xs font-medium text-[var(--text)] truncate">
                  {file.name}
                </p>
                <p className="text-[11px] font-mono text-[var(--text-tertiary)]">
                  {(file.size / (1024 * 1024)).toFixed(2)} MB
                </p>
              </div>
            </div>

            <button
              type="button"
              onClick={handleRemoveFile}
              className="p-1.5 rounded-[6px] text-[var(--text-tertiary)] hover:text-[var(--danger)] hover:bg-[rgba(239,68,68,0.12)] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] cursor-pointer"
              aria-label="Remove selected resume"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Optional inputs grid: Target role & Years of experience */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label
                htmlFor="target-role"
                className="block text-xs font-medium text-[var(--text-secondary)] mb-1"
              >
                Target role
              </label>
              <input
                id="target-role"
                type="text"
                value={targetRole}
                onChange={(e) => setTargetRole(e.target.value)}
                placeholder="e.g. Senior Java Developer"
                className="w-full px-3 py-2 bg-[var(--bg-page)] border border-[var(--border)] rounded-[9px] text-xs text-[var(--text)] placeholder-[var(--text-tertiary)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] transition-colors"
              />
            </div>

            <div>
              <label
                htmlFor="experience-years"
                className="block text-xs font-medium text-[var(--text-secondary)] mb-1"
              >
                Years of experience
              </label>
              <input
                id="experience-years"
                type="number"
                min="0"
                max="60"
                value={experienceYears}
                onChange={(e) => setExperienceYears(e.target.value)}
                placeholder="e.g. 5"
                className="w-full px-3 py-2 bg-[var(--bg-page)] border border-[var(--border)] rounded-[9px] text-xs text-[var(--text)] placeholder-[var(--text-tertiary)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] transition-colors"
              />
            </div>
          </div>

          {/* Required Job Description textarea */}
          <div>
            <div className="flex items-center justify-between mb-1">
              <label
                htmlFor="job-description"
                className="text-xs font-medium text-[var(--text-secondary)]"
              >
                Job description <span className="text-[var(--brand-500)]">*</span>
              </label>
              <span className="text-[11px] font-mono text-[var(--text-tertiary)]">
                Required for keyword scoring
              </span>
            </div>
            <textarea
              id="job-description"
              required
              rows={5}
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              placeholder="Paste the job description text here..."
              className="w-full p-3 bg-[var(--bg-page)] border border-[var(--border)] rounded-[9px] text-xs text-[var(--text)] placeholder-[var(--text-tertiary)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] transition-colors resize-none leading-relaxed"
            />
          </div>

          {/* Submission button */}
          <button
            type="submit"
            disabled={isSubmitting || !jobDescription.trim()}
            className="w-full py-2.5 px-4 rounded-[9px] text-xs font-semibold bg-[var(--brand-500)] hover:bg-[var(--brand-600)] disabled:opacity-50 disabled:cursor-not-allowed text-[var(--on-brand)] transition-all duration-150 shadow-[var(--shadow-primary-hover)] focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--brand-400)] cursor-pointer"
          >
            Analyze resume
          </button>
        </form>
      )}
    </div>
  );
}
