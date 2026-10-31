export const VERDICTS = {
  EXCELLENT: {
    tier: 'EXCELLENT',
    headline: 'Strong match for this job',
    body: 'Your resume lines up well with what this job asks for. A little polish will keep it competitive.',
  },
  STRONG: {
    tier: 'STRONG',
    headline: 'Good match, with room to improve',
    body: 'Most of what the job asks for is on your resume. Fix the items below to make it stronger.',
  },
  MODERATE: {
    tier: 'MODERATE',
    headline: 'Partial match',
    body: 'Your resume covers some of what the job asks for, but important gaps remain.',
  },
  WEAK: {
    tier: 'WEAK',
    headline: 'Not a strong match yet',
    body: 'Your resume is missing several things this job asks for. The action plan shows where to start.',
  },
  POOR: {
    tier: 'POOR',
    headline: 'Weak match for this job',
    body: 'Your resume has little in common with this job right now. Start with the action plan, or try a job description that is closer to your experience.',
  },
};

export const CATEGORIES = {
  ATS_COMPATIBILITY: {
    label: 'ATS compatibility',
    weight: 25,
    what: "Whether screening software can read your resume: clear headings, contact details, a simple layout and enough of the job's skills.",
    good: 'Software should read your resume without problems.',
    partial: 'Software can read most of your resume, but a few things may be missed.',
    needs: 'Parts of your resume may be missed or misread by screening software.',
    tips: [
      'Use plain headings such as Education, Experience, Projects and Skills.',
      'Put your email and phone number at the top as plain text.',
      'Use one column and avoid tables and text boxes.',
    ],
  },
  SKILLS_VALIDATION: {
    label: 'Skills match',
    weight: 25,
    what: 'How many of the skills in the job description appear on your resume.',
    good: 'You cover most of the skills this job asks for.',
    partial: 'You cover some of the skills this job asks for.',
    needs: 'Many skills from the job description are missing from your resume.',
    tips: [
      'Add the missing skills you genuinely have.',
      'Use the same names as the job description, for example Node.js.',
      'Show each skill in a project or job bullet, not only in a skills list.',
    ],
  },
  IMPACT_ACHIEVEMENTS: {
    label: 'Impact and results',
    weight: 20,
    what: 'Whether your bullet points show results, with numbers and strong action verbs.',
    good: 'Your bullets show clear results.',
    partial: 'Some bullets show results, others only describe duties.',
    needs: 'Your bullets describe duties more than results.',
    tips: [
      'Start each bullet with an action verb such as built, led or reduced.',
      'Add a number where you can: users, percent, time saved or team size.',
      'Say what changed because of your work.',
    ],
  },
  PROJECT_QUALITY: {
    label: 'Project quality',
    weight: 15,
    what: 'Whether each project has a title, the tools you used, a clear description and a result.',
    good: 'Your projects are clear and complete.',
    partial: 'Your projects are mostly clear, with a few gaps.',
    needs: 'Your projects are missing details recruiters look for.',
    tips: [
      'Give each project a short, clear title.',
      'List the technologies you used.',
      'Write at least 10 words about what you built and add one result.',
    ],
  },
  WRITING_QUALITY: {
    label: 'Writing quality',
    weight: 10,
    what: 'Whether your sentences are clear and direct, without filler phrases, repeated words or passive wording.',
    good: 'Your writing is clear and direct.',
    partial: 'Your writing is fine, with a few weak spots.',
    needs: 'Your writing may be hard to skim.',
    tips: [
      'Keep each bullet to one idea.',
      'Replace phrases like responsible for or worked on with action verbs.',
      'Drop I, me and my. Start with the action.',
    ],
  },
  PROFESSIONALISM: {
    label: 'Professionalism',
    weight: 5,
    what: 'Whether dates and bullets are consistent, your email looks professional, spelling is correct and the length is sensible.',
    good: 'Your resume looks consistent and professional.',
    partial: 'Your resume looks professional, with small inconsistencies.',
    needs: 'Small inconsistencies may hurt the first impression.',
    tips: [
      'Use one date format everywhere.',
      'Use one bullet style.',
      'Use a simple email address based on your name.',
      'Keep the resume to one or two pages.',
    ],
  },
};

export const EFFORT = {
  QUICK: 'Quick fix',
  CONTENT: 'Add content',
  REWRITE: 'Needs rewriting',
};

export const IMPORTANCE = {
  HIGH: 'High importance',
  MEDIUM: 'Medium importance',
  LOW: 'Small polish',
};

export const RISK_COPY = {
  MISSING_SECTIONS: {
    title: 'Some standard headings are missing',
    what: 'We could not find all the usual headings, such as Education, Experience, Projects and Skills.',
    why: 'Screening software uses headings to sort your information. Without them, parts may be skipped.',
    fix: 'Add the missing headings using plain names.',
    effort: 'QUICK',
    example: {
      before: 'What I have built',
      after: 'Projects',
    },
  },
  NO_CONTACT_INFO: {
    title: 'No email or phone number found',
    what: 'We could not find an email address or phone number.',
    why: 'Recruiters cannot reach you if the software cannot find your details.',
    fix: 'Put your email and phone number at the top of the page as plain text, not inside an image.',
    effort: 'QUICK',
  },
  MULTI_COLUMN_TABLE: {
    title: 'The layout may confuse screening software',
    what: 'Your resume seems to use columns or tables.',
    why: 'Software reads text in one line order. Columns can mix up your sentences.',
    fix: 'Switch to a single-column layout. Avoid tables and text boxes.',
    effort: 'REWRITE',
  },
  GRAPHICS_PRESENT: {
    title: 'Large images found',
    what: 'We found images larger than about one inch.',
    why: 'Text inside images cannot be read by screening software.',
    fix: 'Remove large images and logos. Small icons are fine.',
    effort: 'QUICK',
  },
  KEYWORD_DENSITY_BELOW_30: {
    title: "Few of the job's skills appear",
    what: "Fewer than 3 in 10 skills from the job description appear on your resume.",
    why: 'Screening often starts by matching skills. A low match can push your resume down the list.',
    fix: "Add the skills you genuinely have, using the job's wording, inside your experience and project bullets.",
    effort: 'CONTENT',
  },
  KEYWORD_DENSITY_30_TO_50: {
    title: "Some of the job's skills are missing",
    what: 'Between 3 and 5 in 10 skills from the job description appear on your resume.',
    why: 'More matching skills usually means a better match ranking.',
    fix: 'Add the missing skills you genuinely have, inside your experience and project bullets.',
    effort: 'CONTENT',
  },
  NO_MEASURABLE_ACHIEVEMENTS: {
    title: 'No numbers or results found',
    what: 'We found no numbers that show results, such as users, percentages or team size.',
    why: 'Numbers make your impact concrete and easy to scan.',
    fix: 'Add a number to your strongest bullets.',
    effort: 'CONTENT',
    example: {
      before: 'Built a login system',
      after: 'Built a login system used by 6,000 students',
    },
  },
  FIRST_PERSON: {
    title: 'Bullets use I, me or my',
    what: 'We found first-person words such as I, me, my, we or our.',
    why: 'Resumes read stronger when each bullet starts with an action.',
    fix: 'Remove the pronoun and start with a verb. Do not rewrite in the third person.',
    effort: 'QUICK',
    example: {
      before: 'I built the website for my college',
      after: 'Built the college website',
    },
  },
  FILLER_WORDS: {
    title: 'Weak phrases found',
    what: 'We found phrases such as responsible for or worked on.',
    why: 'They describe duties, not results, and hide what you did.',
    fix: 'Replace them with a direct action verb such as led, built, improved or designed.',
    effort: 'QUICK',
    example: {
      before: 'Responsible for testing the app',
      after: 'Tested the app before every release',
    },
  },
  REPEATED_WORDS: {
    title: 'Some words repeat a lot',
    what: 'Some words appear many times.',
    why: 'Repeating the same word makes writing feel flat.',
    fix: 'Use different verbs and describe different details.',
    effort: 'QUICK',
  },
  LONG_SENTENCES: {
    title: 'Sentences are long',
    what: 'Your sentences are long on average.',
    why: 'Recruiters skim. Short bullets get read, long ones get skipped.',
    fix: 'Keep each bullet to one idea, ideally under 20 words.',
    effort: 'REWRITE',
  },
  PASSIVE_VOICE: {
    title: 'Too much passive wording',
    what: 'Many sentences hide who did the work, such as was built by.',
    why: 'Active wording is shorter and shows ownership.',
    fix: 'Write the action first.',
    effort: 'QUICK',
    example: {
      before: 'The API was built by me',
      after: 'Built the API',
    },
  },
  SPELLING_ERRORS: {
    title: 'Possible spelling mistakes',
    what: 'We found words that may be misspelled.',
    why: 'Typos can suggest a lack of care. Some are false alarms, such as tool names.',
    fix: 'Run a spell check and ignore technical terms that are spelled correctly.',
    effort: 'QUICK',
  },
  INCONSISTENT_DATES: {
    title: 'Dates use mixed formats',
    what: 'We found more than one date format, such as Jan 2024 and 01/2024.',
    why: 'Mixed formats look careless.',
    fix: 'Pick one format and use it everywhere.',
    effort: 'QUICK',
  },
  INCONSISTENT_BULLETS: {
    title: 'Bullet styles are mixed',
    what: 'We found more than one bullet symbol.',
    why: 'One style looks cleaner and is easier to read.',
    fix: 'Use one bullet symbol throughout.',
    effort: 'QUICK',
  },
  CASUAL_EMAIL: {
    title: 'The email address looks informal',
    what: 'Your email address looks casual.',
    why: 'First impressions matter. A name-based address looks more professional.',
    fix: 'Use an address based on your name.',
    effort: 'QUICK',
  },
  OVER_700_WORDS: {
    title: 'The resume may be too long',
    what: 'Your resume has more than 700 words, about two pages.',
    why: 'Recruiters spend little time on each resume.',
    fix: 'Keep the strongest and most relevant items. If you are early in your career, aim for one page.',
    effort: 'REWRITE',
  },
};

export const RISKS = RISK_COPY;

export const TIER_RANGES = {
  EXCELLENT: '85 and above',
  STRONG: '70 to 84',
  MODERATE: '55 to 69',
  WEAK: '40 to 54',
  POOR: 'below 40',
};

export const DISCLAIMER =
  "These scores are estimates based on fixed rules. They do not guarantee how any company's system will treat your resume.";

export const GLOSSARY = {
  ATS: 'Applicant tracking system, software many companies use to sort resumes before a person reads them.',
  ats: 'Applicant tracking system, software many companies use to sort resumes before a person reads them.',
  keyword: 'A skill or term from the job description.',
  tier: 'The label for a score range, such as Strong match.',
};

export default {
  VERDICTS,
  CATEGORIES,
  EFFORT,
  IMPORTANCE,
  RISK_COPY,
  RISKS,
  TIER_RANGES,
  DISCLAIMER,
  GLOSSARY,
};

