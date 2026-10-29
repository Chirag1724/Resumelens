import { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { FileText, X, AlertCircle, Loader2, CheckCircle2, Upload, Sparkles } from 'lucide-react';
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
      className={`relative rounded-[2.5rem] transition-all duration-500 bg-[rgba(10,10,14,0.20)] backdrop-blur-xl ${
        isDragging ? 'scale-[1.02]' : ''
      } ${
        !file
          ? 'p-8 sm:p-10 min-h-[460px] flex flex-col items-center justify-center'
          : 'p-6 sm:p-7 md:p-8'
      } ${className}`}
    >
      {/* Dashed orange border */}
      <div className="absolute inset-0 rounded-[2.5rem] border-[1.5px] border-dashed border-[#E8602E]/60 shadow-[0_0_40px_rgba(232,96,46,0.15),inset_0_0_20px_rgba(232,96,46,0.05)] pointer-events-none" />

      {/* Hidden file input */}
      <input
        ref={fileInputRef}
        type="file"
        id="resume-upload"
        accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        onChange={handleFileInputChange}
        className="hidden"
        aria-label="Upload resume file"
      />

      {/* Error notification banner */}
      <AnimatePresence>
        {errorMessage && (
          <motion.div
            initial={{ opacity: 0, y: -8, scale: 0.98 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, y: -8, scale: 0.98 }}
            transition={{ duration: 0.2 }}
            role="alert"
            className="relative z-20 mb-4 p-3 rounded-xl border border-red-500/40 bg-red-500/10 text-red-400 text-xs flex items-start gap-2.5"
          >
            <AlertCircle className="w-4 h-4 shrink-0 mt-0.5" />
            <div className="flex-1 leading-relaxed">{errorMessage}</div>
            <button
              type="button"
              onClick={() => setErrorMessage(null)}
              className="text-red-400 hover:opacity-80 p-0.5"
              aria-label="Dismiss error"
            >
              <X className="w-3.5 h-3.5" />
            </button>
          </motion.div>
        )}
      </AnimatePresence>

      <AnimatePresence mode="wait">
        {/* STATE 1: Analyzing / Scanning progression */}
        {isSubmitting ? (
          <motion.div
            key="state-submitting"
            initial={{ opacity: 0, scale: 0.98 }}
            animate={{ opacity: 1, scale: 1 }}
            exit={{ opacity: 0, scale: 0.98 }}
            transition={{ duration: 0.22 }}
            className="flex flex-col items-center justify-center py-8 z-10 w-full px-4"
          >
            <div className="w-full max-w-xs mb-6">
              <DocumentMockup scanning={true} />
            </div>

            <div className="w-full max-w-sm bg-[#111111] border border-white/10 rounded-2xl p-5 text-center shadow-xl">
              <div className="flex items-center justify-center gap-6 mb-3">
                <div className="flex items-center gap-2">
                  {currentStep === 'Uploading' ? (
                    <Loader2 className="w-4 h-4 text-[#E8602E] animate-spin" />
                  ) : (
                    <CheckCircle2 className="w-4 h-4 text-[#10b981]" />
                  )}
                  <span className={`text-xs font-semibold ${currentStep === 'Uploading' ? 'text-[#E8602E]' : 'text-white/60'}`}>
                    Uploading
                  </span>
                </div>

                <span className="text-white/20">/</span>

                <div className="flex items-center gap-2">
                  {currentStep === 'Analyzing' ? (
                    <Loader2 className="w-4 h-4 text-[#E8602E] animate-spin" />
                  ) : (
                    <div className="w-3 h-3 rounded-full border-2 border-white/20" />
                  )}
                  <span className={`text-xs font-semibold ${currentStep === 'Analyzing' ? 'text-[#E8602E]' : 'text-white/30'}`}>
                    Analyzing
                  </span>
                </div>
              </div>

              <p className="text-xs text-white/50">
                {currentStep === 'Uploading'
                  ? 'Extracting resume text and verifying file structure...'
                  : 'Scoring six dimensions and computing keyword matches...'}
              </p>
            </div>
          </motion.div>
        ) : !file ? (
          /* STATE 2: Empty intake drop zone */
          <motion.label
            key="state-empty"
            htmlFor="resume-upload"
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 0.2 }}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            onKeyDown={handleKeyDown}
            tabIndex={0}
            role="button"
            aria-label="Drag and drop resume here or click to browse"
            className="flex flex-col items-center cursor-pointer text-center w-full relative z-10 select-none outline-none"
          >
            {/* PdfIcon */}
            <div className="mb-6 transform transition-transform duration-200 hover:scale-105">
              <PdfIcon size={64} />
            </div>

            <h3 className="text-2xl md:text-3xl font-bold mb-3 tracking-tight text-white/90">
              {isDragging ? 'Drop your resume here' : 'Drag & drop your resume here'}
            </h3>

            <p className="text-white/40 text-lg mb-8 font-medium">
              or click to browse
            </p>

            <div className="px-12 py-3.5 border border-[#E8602E]/40 bg-[#E8602E]/[0.02] hover:bg-[#E8602E]/10 text-white text-[15px] font-bold rounded-xl flex items-center gap-3 transition-all duration-300 pointer-events-none shadow-[0_0_30px_rgba(232,96,46,0.1)]">
              <Upload className="w-5 h-5 text-[#E8602E]" />
              <span>Upload Resume</span>
            </div>

            <p className="text-sm text-white/30 mt-8 font-medium">
              Supported formats: PDF, DOCX &bull; Max 10MB
            </p>
          </motion.label>
        ) : (
          /* STATE 3: Selected file transformed form with perfected spacing and modern styling */
          <motion.form
            key="state-form"
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            exit={{ opacity: 0, y: -8 }}
            transition={{ duration: 0.22 }}
            onSubmit={handleSubmit}
            className="w-full space-y-4 sm:space-y-4.5 relative z-10"
          >
          {/* File chip with remove button */}
          <div className="flex items-center justify-between p-3.5 sm:p-4 rounded-2xl bg-[#141418] border border-white/10 shadow-lg">
            <div className="flex items-center gap-3.5 overflow-hidden min-w-0">
              <div className="w-10 h-10 rounded-xl bg-[#E8602E]/15 border border-[#E8602E]/30 flex items-center justify-center text-[#ff6b35] shrink-0 shadow-sm">
                <FileText className="w-5 h-5 text-[#E8602E]" />
              </div>
              <div className="overflow-hidden text-left min-w-0">
                <div className="flex items-center gap-2">
                  <p className="text-sm sm:text-[15px] font-semibold text-white truncate max-w-[200px] sm:max-w-xs">
                    {file.name}
                  </p>
                  <span className="px-1.5 py-0.5 text-[10px] font-mono font-bold uppercase rounded bg-white/10 text-white/70 shrink-0">
                    {file.name.split('.').pop() || 'PDF'}
                  </span>
                </div>
                <div className="flex items-center gap-2 text-xs text-neutral-400 mt-1">
                  <span className="font-mono">{(file.size / (1024 * 1024)).toFixed(2)} MB</span>
                  <span className="text-neutral-600">&bull;</span>
                  <span className="inline-flex items-center gap-1.5 text-emerald-400 font-medium">
                    <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse" />
                    Ready to analyze
                  </span>
                </div>
              </div>
            </div>

            <button
              type="button"
              onClick={handleRemoveFile}
              className="p-2 rounded-xl text-neutral-400 hover:text-red-400 hover:bg-red-500/10 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#E8602E] cursor-pointer shrink-0 ml-2"
              aria-label="Remove selected resume"
              title="Remove resume"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          {/* Inputs grid: Target role & Years of experience */}
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3.5 sm:gap-4 text-left">
            <div>
              <div className="flex items-center justify-between mb-2">
                <label
                  htmlFor="target-role"
                  className="block text-xs font-semibold text-neutral-200"
                >
                  Target role
                </label>
                <span className="text-[10px] font-mono text-neutral-500 uppercase tracking-wider">
                  Optional
                </span>
              </div>
              <input
                id="target-role"
                type="text"
                value={targetRole}
                onChange={(e) => setTargetRole(e.target.value)}
                placeholder="e.g. Senior Java Developer"
                className="w-full px-4 py-2.5 sm:py-3 bg-[#111114] border border-white/10 hover:border-white/20 focus:border-[#E8602E] focus:ring-1 focus:ring-[#E8602E] rounded-xl text-sm text-white placeholder-neutral-500 focus-visible:outline-none transition-all"
              />
            </div>

            <div>
              <div className="flex items-center justify-between mb-2">
                <label
                  htmlFor="experience-years"
                  className="block text-xs font-semibold text-neutral-200"
                >
                  Years of experience
                </label>
                <span className="text-[10px] font-mono text-neutral-500 uppercase tracking-wider">
                  Optional
                </span>
              </div>
              <input
                id="experience-years"
                type="number"
                min="0"
                max="60"
                value={experienceYears}
                onChange={(e) => setExperienceYears(e.target.value)}
                placeholder="e.g. 5"
                className="w-full px-4 py-2.5 sm:py-3 bg-[#111114] border border-white/10 hover:border-white/20 focus:border-[#E8602E] focus:ring-1 focus:ring-[#E8602E] rounded-xl text-sm text-white placeholder-neutral-500 focus-visible:outline-none transition-all"
              />
            </div>
          </div>

          {/* Required Job Description textarea */}
          <div className="text-left">
            <div className="flex items-center justify-between mb-2">
              <label
                htmlFor="job-description"
                className="text-xs font-semibold text-neutral-200 flex items-center gap-1.5"
              >
                Job description
                <span className="text-[#E8602E] font-bold text-sm">*</span>
              </label>
              <span className="text-[11px] font-mono px-2 py-0.5 rounded-full bg-[#E8602E]/10 border border-[#E8602E]/25 text-[#ff8b5c]">
                Required for keyword scoring
              </span>
            </div>
            <textarea
              id="job-description"
              required
              rows={4}
              value={jobDescription}
              onChange={(e) => setJobDescription(e.target.value)}
              placeholder="Paste the job description text here..."
              className="w-full p-4 bg-[#111114] border border-white/10 hover:border-white/20 focus:border-[#E8602E] focus:ring-1 focus:ring-[#E8602E] rounded-xl text-sm text-white placeholder-neutral-500 focus-visible:outline-none transition-all resize-none leading-relaxed min-h-[110px]"
            />
          </div>

          {/* Submission button */}
          <div className="pt-1 sm:pt-1.5">
            <button
              type="submit"
              disabled={isSubmitting || !jobDescription.trim()}
              className={`w-full py-3.5 sm:py-4 px-6 rounded-xl text-sm sm:text-base font-bold flex items-center justify-center gap-2.5 transition-all duration-200 ${
                isSubmitting || !jobDescription.trim()
                  ? 'bg-[#18181c] border border-white/10 text-neutral-400 cursor-not-allowed opacity-90'
                  : 'bg-gradient-to-r from-[#ff6b35] via-[#E8602E] to-[#e8551e] hover:from-[#ff7a45] hover:to-[#ea580c] text-white shadow-[0_4px_28px_rgba(232,96,46,0.45)] hover:shadow-[0_6px_36px_rgba(232,96,46,0.65)] cursor-pointer transform hover:-translate-y-0.5 active:translate-y-0'
              }`}
            >
              {isSubmitting ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin text-[#E8602E]" />
                  <span>Analyzing resume...</span>
                </>
              ) : (
                <>
                  <Sparkles
                    className={`w-4 h-4 transition-colors ${
                      !jobDescription.trim() ? 'text-neutral-500' : 'text-white'
                    }`}
                  />
                  <span>Analyze resume</span>
                </>
              )}
            </button>
          </div>
        </motion.form>
      )}
      </AnimatePresence>
    </div>
  );
}
