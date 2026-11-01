export const SKILL_DISPLAY_NAMES = {
  'react': 'React',
  'next.js': 'Next.js',
  'typescript': 'TypeScript',
  'javascript': 'JavaScript',
  'node': 'Node.js',
  'express': 'Express',
  'mongodb': 'MongoDB',
  'postgresql': 'PostgreSQL',
  'mysql': 'MySQL',
  'jwt': 'JWT',
  'ci/cd': 'CI/CD',
  'github actions': 'GitHub Actions',
  'aws': 'AWS',
  'graphql': 'GraphQL',
  'css': 'CSS',
  'html': 'HTML',
  'rest': 'REST',
  'apis': 'APIs',
  'tailwind': 'Tailwind CSS',
  'git': 'Git',
  'linux': 'Linux',
  'docker': 'Docker',
  'redis': 'Redis',
  'kubernetes': 'Kubernetes',
  'jest': 'Jest',
  'python': 'Python',
  'sql': 'SQL',
  'pytorch': 'PyTorch',
  'tensorflow': 'TensorFlow',
  'pandas': 'pandas',
  'nlp': 'NLP',
  'mlops': 'MLOps',
};

export function prettySkill(keyword) {
  if (!keyword || typeof keyword !== 'string') {
    return '';
  }
  const trimmed = keyword.trim();
  const lower = trimmed.toLowerCase();
  if (SKILL_DISPLAY_NAMES[lower]) {
    return SKILL_DISPLAY_NAMES[lower];
  }
  // For any other keyword: capitalize the first letter when the keyword is all lowercase and longer than 3 characters, otherwise leave it.
  if (trimmed === lower && trimmed.length > 3) {
    return trimmed.charAt(0).toUpperCase() + trimmed.slice(1);
  }
  return trimmed;
}

export default prettySkill;

