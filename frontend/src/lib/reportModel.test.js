import test from 'node:test';
import assert from 'node:assert/strict';

import {
  tierFor,
  verdictFor,
  QUALITY_ONLY_VERDICT,
  capInfo,
  pointsGain,
  pointsEarned,
  keywordBand,
  summaryFacts,
  buildActionPlan,
  scaleMarkerPosition,
  SCALE_SEGMENT_WIDTHS,
} from './reportModel.js';
import { formatScore } from './format.js';
import { prettySkill, SKILL_DISPLAY_NAMES } from './skillNames.js';
import { VERDICTS, SUGGESTION_COPY } from '../content/reportCopy.js';

test('formatScore rounds to 1 decimal and drops trailing .0', () => {
  assert.strictEqual(formatScore(54.62), '54.6');
  assert.strictEqual(formatScore(69), '69');
  assert.strictEqual(formatScore(88.16), '88.2');
  assert.strictEqual(formatScore(100), '100');
  assert.strictEqual(formatScore('54.62'), '54.6');
  assert.strictEqual(formatScore(0), '0');
  assert.strictEqual(formatScore(54.0), '54');
});

test('exact score evaluation: 54.62 is WEAK and not rounded to MODERATE', () => {
  assert.strictEqual(tierFor(54.62), 'WEAK');
  assert.strictEqual(verdictFor(54.62).tier, 'WEAK');
});

test('pointsEarned uses exact score so 54.4 with weight 25 gives 13.6 of 25', () => {
  assert.strictEqual(pointsEarned(54.4, 25), '13.6 of 25');
  assert.strictEqual(pointsEarned(85, 25), '21.3 of 25');
});

test('scale segment widths and marker positions', () => {
  assert.deepStrictEqual(SCALE_SEGMENT_WIDTHS, [40, 15, 15, 15, 15]);
  assert.strictEqual(scaleMarkerPosition(69), 69);
  assert.strictEqual(scaleMarkerPosition(40), 40);
  assert.strictEqual(scaleMarkerPosition(0), 0);
  assert.strictEqual(scaleMarkerPosition(100), 100);
});

test('cap plan item and capInfo with threshold 40', () => {
  const data = {
    scoreCapped: true,
    overallScore: 54,
    uncappedScore: 72.5,
    skillsScore: 16,
    sectionScores: [
      {
        sectionName: 'SKILLS_VALIDATION',
        score: 16,
        matchedKeywords: ['react', 'node', 'git'],
        missingKeywords: [
          'aws', 'docker', 'k8s', 'sql', 'nosql', 'java', 'python', 'go',
          'redis', 'graphql', 'rest', 'ci/cd', 'tdd', 'linux', 'kafka', 'next',
        ],
      },
    ],
  };

  const info = capInfo(data);
  assert.notStrictEqual(info, null);
  assert.strictEqual(info.threshold, 40);
  assert.strictEqual(info.moreSkills, 5);
  assert.strictEqual(info.cap, 54);
  assert.strictEqual(info.uncapped, 72.5);
  assert.strictEqual(info.skillsNow, 16);

  const plan = buildActionPlan(data);
  assert.strictEqual(plan[0].id, 'cap');
  assert.strictEqual(
    plan[0].why,
    'Your score is held at 54 because your skills match is below 40.'
  );
  assert.strictEqual(
    plan[0].how,
    'Add about 5 more of the missing skills you genuinely have, inside your bullets.'
  );
  assert.strictEqual(plan[0].gainText, 'Could lift your score from 54 to up to 69');
});

test('cap plan item with threshold 60 and null moreSkills', () => {
  const data = {
    scoreCapped: true,
    overallScore: 69,
    uncappedScore: 78.4,
    skillsScore: 50,
    sectionScores: [
      {
        sectionName: 'SKILLS_VALIDATION',
        score: 50,
        matchedKeywords: [],
        missingKeywords: [],
      },
    ],
  };

  const info = capInfo(data);
  assert.strictEqual(info.threshold, 60);
  assert.strictEqual(info.moreSkills, null);

  const plan = buildActionPlan(data);
  assert.strictEqual(plan[0].id, 'cap');
  assert.strictEqual(
    plan[0].why,
    'Your score is held at 69 because your skills match is below 60.'
  );
  assert.strictEqual(
    plan[0].how,
    'Add the missing skills you genuinely have, inside your bullets.'
  );
  assert.strictEqual(
    plan[0].gainText,
    'Could lift your score from 69 to about 78.4'
  );
});

test('verdictFor(85) is EXCELLENT and verdictFor(84.99) is STRONG', () => {
  assert.strictEqual(verdictFor(85), VERDICTS.EXCELLENT);
  assert.strictEqual(verdictFor(85).tier, 'EXCELLENT');
  assert.strictEqual(verdictFor(84.99), VERDICTS.STRONG);
  assert.strictEqual(verdictFor(84.99).tier, 'STRONG');
  assert.strictEqual(verdictFor(70).tier, 'STRONG');
  assert.strictEqual(verdictFor(69.99).tier, 'MODERATE');
  assert.strictEqual(verdictFor(55).tier, 'MODERATE');
  assert.strictEqual(verdictFor(54.99).tier, 'WEAK');
  assert.strictEqual(verdictFor(40).tier, 'WEAK');
  assert.strictEqual(verdictFor(39.99).tier, 'POOR');
});

test('verdictFor returns quality-only variant when both keyword lists are empty', () => {
  const quality = verdictFor(85, true);
  assert.strictEqual(quality, QUALITY_ONLY_VERDICT);
  assert.strictEqual(quality.headline, 'Resume quality check');
  assert.strictEqual(
    quality.body,
    'We checked your formatting, writing and results. Paste the full job posting to see how well you match the job.'
  );
  assert.strictEqual(
    quality.note,
    "This score reflects your resume's quality only. We could not find specific skills in the job description, so the job match is not included."
  );

  // Calling with empty keyword arrays
  assert.strictEqual(verdictFor(75, [], []), QUALITY_ONLY_VERDICT);

  // Calling with an evaluation data object where skills section has empty lists
  const emptyData = {
    overallScore: 82,
    sectionScores: [
      { sectionName: 'SKILLS_VALIDATION', matchedKeywords: [], missingKeywords: [] },
    ],
  };
  assert.strictEqual(verdictFor(emptyData), QUALITY_ONLY_VERDICT);
  assert.strictEqual(verdictFor(82, emptyData), QUALITY_ONLY_VERDICT);
});

test('verdictFor returns normal verdict when keywords are present or when called with only score', () => {
  // Score only defaults to normal tier verdict
  assert.strictEqual(verdictFor(85), VERDICTS.EXCELLENT);
  assert.strictEqual(verdictFor(70), VERDICTS.STRONG);
  assert.strictEqual(verdictFor(55), VERDICTS.MODERATE);

  // Non-empty keyword lists
  assert.strictEqual(verdictFor(85, ['react'], []), VERDICTS.EXCELLENT);
  assert.strictEqual(verdictFor(70, [], ['docker']), VERDICTS.STRONG);
  assert.strictEqual(verdictFor(55, ['git'], ['docker']), VERDICTS.MODERATE);

  // Data object with skills keywords present
  const dataWithKeywords = {
    overallScore: 85,
    sectionScores: [
      { sectionName: 'SKILLS_VALIDATION', matchedKeywords: ['react'], missingKeywords: [] },
    ],
  };
  assert.strictEqual(verdictFor(dataWithKeywords), VERDICTS.EXCELLENT);
  assert.strictEqual(verdictFor(85, dataWithKeywords), VERDICTS.EXCELLENT);

  // Explicit false flag
  assert.strictEqual(verdictFor(85, false), VERDICTS.EXCELLENT);
});

test('suggestion copy rules for WEAK_VERBS', () => {
  const data = {
    scoreCapped: false,
    suggestions: [
      {
        code: 'WEAK_VERBS',
        priority: 'HIGH',
        title: 'API Weak Verb Title',
        description: 'API Weak Verb Description',
        example: 'Responsible for leading team deployments',
      },
    ],
  };

  const plan = buildActionPlan(data);
  const item = plan.find((i) => i.id === 'WEAK_VERBS');
  assert.notStrictEqual(item, undefined);
  assert.strictEqual(item.title, 'Replace weak phrases');
  assert.strictEqual(
    item.why,
    'Phrases like worked on or responsible for describe duties, not results.'
  );
  assert.strictEqual(
    item.how,
    'Start each bullet with a direct action verb such as led, built or improved.'
  );
  assert.strictEqual(item.effort, 'Quick fix');
  assert.strictEqual(item.fromResume, 'Responsible for leading team deployments');
  assert.strictEqual(item.example, null);
});

test('suggestion copy rules for QUANTIFY with parsed bullets count and impact under 85', () => {
  const data = {
    scoreCapped: false,
    sectionScores: [
      { sectionName: 'IMPACT_ACHIEVEMENTS', score: 70 },
    ],
    suggestions: [
      {
        code: 'QUANTIFY',
        priority: 'MEDIUM',
        title: 'API Quantify Title',
        description: '4 bullet point(s) lack measurable outcomes.',
        example: 'Reduced build time by 40%',
      },
    ],
  };

  const plan = buildActionPlan(data);
  const item = plan.find((i) => i.id === 'QUANTIFY');
  assert.notStrictEqual(item, undefined);
  assert.strictEqual(item.title, 'Add numbers to your bullets');
  assert.strictEqual(
    item.why,
    'Numbers make your results concrete and easy to scan. 4 of your bullets have no number.'
  );
  assert.strictEqual(
    item.how,
    'Pick your three strongest bullets and add a number to each: users, percent, time saved or team size.'
  );
  assert.strictEqual(item.effort, 'Add content');
  assert.strictEqual(item.fromResume, null);
  assert.deepStrictEqual(item.example, {
    before: 'Built a login system',
    after: 'Built a login system used by 6,000 students',
  });
  assert.strictEqual(item.exampleLabel, 'Example');
});

test('suggestion copy rules for QUANTIFY with 1 bullet and impact score 85 or higher', () => {
  const data = {
    scoreCapped: false,
    sectionScores: [
      { sectionName: 'IMPACT_ACHIEVEMENTS', score: 88 },
    ],
    suggestions: [
      {
        code: 'QUANTIFY',
        priority: 'MEDIUM',
        title: 'API Quantify Title',
        description: '1 bullet point lacks measurable outcomes.',
        example: 'Some API line',
      },
    ],
  };

  const plan = buildActionPlan(data);
  const item = plan.find((i) => i.id === 'QUANTIFY');
  assert.notStrictEqual(item, undefined);
  assert.strictEqual(item.importance, 'Small polish');
  assert.strictEqual(
    item.why,
    'Optional. Your bullets already show clear results, so this is polish. Numbers make your results concrete and easy to scan. 1 of your bullets has no number.'
  );
});

test('suggestion copy rules for STRUCTURE', () => {
  const data = {
    scoreCapped: false,
    suggestions: [
      {
        code: 'STRUCTURE',
        priority: 'LOW',
        title: 'API Structure Title',
        description: 'API Structure Description',
        example: 'Some structure example',
      },
    ],
  };

  const plan = buildActionPlan(data);
  const item = plan.find((i) => i.id === 'STRUCTURE');
  assert.notStrictEqual(item, undefined);
  assert.strictEqual(item.title, 'Check your section headings and contact details');
  assert.strictEqual(item.why, 'Your ATS compatibility score is below 70.');
  assert.strictEqual(item.how, 'Use plain headings and put your contact details at the top.');
  assert.strictEqual(item.effort, 'Quick fix');
  assert.strictEqual(item.fromResume, null);
});

test('fromResume may appear only for WEAK_VERBS and merged risk items with a real line', () => {
  const data = {
    scoreCapped: false,
    risks: [
      {
        code: 'FILLER_WORDS',
        severity: 'LOW',
        points: 3,
        section: 'WRITING_QUALITY',
      },
      {
        code: 'NO_MEASURABLE_ACHIEVEMENTS',
        severity: 'MEDIUM',
        points: 6,
        section: 'IMPACT_ACHIEVEMENTS',
      },
    ],
    suggestions: [
      {
        code: 'WEAK_VERBS',
        priority: 'HIGH',
        example: 'Responsible for leading the migration',
      },
      {
        code: 'QUANTIFY',
        priority: 'MEDIUM',
        example: 'Built 10 features',
      },
    ],
  };

  const plan = buildActionPlan(data);
  const fillerItem = plan.find((i) => i.id === 'FILLER_WORDS');
  const quantifyItem = plan.find((i) => i.id === 'NO_MEASURABLE_ACHIEVEMENTS');

  assert.strictEqual(fillerItem.fromResume, 'Responsible for leading the migration');
  assert.strictEqual(quantifyItem.fromResume, null);
});

test('why and how must never be the same sentence when copy is missing', () => {
  const data = {
    scoreCapped: false,
    suggestions: [
      {
        code: 'UNKNOWN_CODE',
        priority: 'LOW',
        title: 'Unknown Title',
        description: 'Unique sentence for unknown item.',
      },
    ],
  };

  const plan = buildActionPlan(data);
  const item = plan.find((i) => i.id === 'UNKNOWN_CODE');
  assert.notStrictEqual(item, undefined);
  assert.notStrictEqual(item.why, item.how);
  assert.strictEqual(item.why, 'Unique sentence for unknown item.');
  assert.strictEqual(item.how, '');
});

test('skillNames map and fallback logic', () => {
  assert.strictEqual(prettySkill('react'), 'React');
  assert.strictEqual(prettySkill('next.js'), 'Next.js');
  assert.strictEqual(prettySkill('typescript'), 'TypeScript');
  assert.strictEqual(prettySkill('javascript'), 'JavaScript');
  assert.strictEqual(prettySkill('node'), 'Node.js');
  assert.strictEqual(prettySkill('express'), 'Express');
  assert.strictEqual(prettySkill('mongodb'), 'MongoDB');
  assert.strictEqual(prettySkill('postgresql'), 'PostgreSQL');
  assert.strictEqual(prettySkill('mysql'), 'MySQL');
  assert.strictEqual(prettySkill('jwt'), 'JWT');
  assert.strictEqual(prettySkill('ci/cd'), 'CI/CD');
  assert.strictEqual(prettySkill('github actions'), 'GitHub Actions');
  assert.strictEqual(prettySkill('aws'), 'AWS');
  assert.strictEqual(prettySkill('graphql'), 'GraphQL');
  assert.strictEqual(prettySkill('css'), 'CSS');
  assert.strictEqual(prettySkill('html'), 'HTML');
  assert.strictEqual(prettySkill('rest'), 'REST');
  assert.strictEqual(prettySkill('apis'), 'APIs');
  assert.strictEqual(prettySkill('tailwind'), 'Tailwind CSS');
  assert.strictEqual(prettySkill('git'), 'Git');
  assert.strictEqual(prettySkill('linux'), 'Linux');
  assert.strictEqual(prettySkill('docker'), 'Docker');
  assert.strictEqual(prettySkill('redis'), 'Redis');
  assert.strictEqual(prettySkill('kubernetes'), 'Kubernetes');
  assert.strictEqual(prettySkill('jest'), 'Jest');
  assert.strictEqual(prettySkill('python'), 'Python');
  assert.strictEqual(prettySkill('sql'), 'SQL');
  assert.strictEqual(prettySkill('pytorch'), 'PyTorch');
  assert.strictEqual(prettySkill('tensorflow'), 'TensorFlow');
  assert.strictEqual(prettySkill('pandas'), 'pandas');
  assert.strictEqual(prettySkill('nlp'), 'NLP');
  assert.strictEqual(prettySkill('mlops'), 'MLOps');

  // Fallback: capitalize first letter when all lowercase and longer than 3 chars
  assert.strictEqual(prettySkill('angular'), 'Angular');
  assert.strictEqual(prettySkill('rust'), 'Rust');
  assert.strictEqual(prettySkill('java'), 'Java');

  // Otherwise leave it
  assert.strictEqual(prettySkill('c++'), 'c++');
  assert.strictEqual(prettySkill('go'), 'go');
  assert.strictEqual(prettySkill('gRPC'), 'gRPC');
  assert.strictEqual(prettySkill(''), '');
});

test('a NO_CONTACT_INFO risk with points 10 in ATS_COMPATIBILITY gives gain 2.5', () => {
  const risk = {
    code: 'NO_CONTACT_INFO',
    points: 10,
    section: 'ATS_COMPATIBILITY',
  };
  assert.strictEqual(pointsGain(risk), 2.5);
});

test('risks LOW 3 points WRITING_QUALITY, HIGH 15 points ATS_COMPATIBILITY and MEDIUM 8 points ATS_COMPATIBILITY order HIGH, MEDIUM, LOW', () => {
  const data = {
    scoreCapped: false,
    risks: [
      {
        code: 'FILLER_WORDS',
        severity: 'LOW',
        points: 3,
        section: 'WRITING_QUALITY',
      },
      {
        code: 'MISSING_SECTIONS',
        severity: 'HIGH',
        points: 15,
        section: 'ATS_COMPATIBILITY',
      },
      {
        code: 'NO_CONTACT_INFO',
        severity: 'MEDIUM',
        points: 8,
        section: 'ATS_COMPATIBILITY',
      },
    ],
  };

  const plan = buildActionPlan(data);
  assert.strictEqual(plan.length, 3);
  assert.strictEqual(plan[0].id, 'MISSING_SECTIONS');
  assert.strictEqual(plan[0].importance, 'High importance');
  assert.strictEqual(plan[1].id, 'NO_CONTACT_INFO');
  assert.strictEqual(plan[1].importance, 'Medium importance');
  assert.strictEqual(plan[2].id, 'FILLER_WORDS');
  assert.strictEqual(plan[2].importance, 'Small polish');
});

test('a response with both a KEYWORDS suggestion and a KEYWORD_DENSITY_BELOW_30 risk yields exactly one skills item', () => {
  const data = {
    scoreCapped: false,
    sectionScores: [
      {
        sectionName: 'SKILLS_VALIDATION',
        score: 25,
        matchedKeywords: ['java'],
        missingKeywords: ['spring', 'docker'],
      },
    ],
    risks: [
      {
        code: 'KEYWORD_DENSITY_BELOW_30',
        severity: 'HIGH',
        points: 15,
        section: 'ATS_COMPATIBILITY',
      },
    ],
    suggestions: [
      {
        code: 'KEYWORDS',
        priority: 'HIGH',
        title: 'Add Missing Technical Keywords',
        description: 'Incorporate missing keywords.',
        example: 'Built services with Spring and Docker.',
      },
    ],
  };

  const plan = buildActionPlan(data);
  const skillsItems = plan.filter(
    (item) => item.id === 'skills' || item.title === 'Add the skills this job asks for'
  );
  assert.strictEqual(skillsItems.length, 1);
  assert.strictEqual(skillsItems[0].title, 'Add the skills this job asks for');
  assert.strictEqual(skillsItems[0].fromResume, null);
});

test('a plan never exceeds 6 items', () => {
  const data = {
    scoreCapped: false,
    risks: [
      { code: 'MISSING_SECTIONS', severity: 'HIGH', points: 20, section: 'ATS_COMPATIBILITY' },
      { code: 'NO_CONTACT_INFO', severity: 'HIGH', points: 15, section: 'ATS_COMPATIBILITY' },
      { code: 'MULTI_COLUMN_TABLE', severity: 'HIGH', points: 10, section: 'ATS_COMPATIBILITY' },
      { code: 'GRAPHICS_PRESENT', severity: 'MEDIUM', points: 8, section: 'ATS_COMPATIBILITY' },
      { code: 'NO_MEASURABLE_ACHIEVEMENTS', severity: 'MEDIUM', points: 6, section: 'IMPACT_ACHIEVEMENTS' },
      { code: 'FIRST_PERSON', severity: 'LOW', points: 4, section: 'WRITING_QUALITY' },
      { code: 'FILLER_WORDS', severity: 'LOW', points: 3, section: 'WRITING_QUALITY' },
      { code: 'REPEATED_WORDS', severity: 'LOW', points: 2, section: 'WRITING_QUALITY' },
    ],
  };

  const plan = buildActionPlan(data);
  assert.strictEqual(plan.length, 6);
});

test('keywordBand(80) is great and keywordBand(79.9) is decent', () => {
  assert.strictEqual(keywordBand(80), 'great');
  assert.strictEqual(keywordBand(79.9), 'decent');
  assert.strictEqual(keywordBand(50), 'decent');
  assert.strictEqual(keywordBand(49.9), 'low');
});

test('an unknown risk code falls back to the API text', () => {
  const data = {
    scoreCapped: false,
    risks: [
      {
        code: 'UNKNOWN_RISK_RULE',
        title: 'Custom API Risk Title',
        description: 'Custom API Risk Description',
        fix: 'Custom API Risk Fix',
        severity: 'MEDIUM',
        points: 5,
        section: 'ATS_COMPATIBILITY',
      },
    ],
  };

  const plan = buildActionPlan(data);
  assert.strictEqual(plan.length, 1);
  assert.strictEqual(plan[0].title, 'Custom API Risk Title');
  assert.strictEqual(plan[0].why, 'Custom API Risk Description');
  assert.strictEqual(plan[0].how, 'Custom API Risk Fix');
});

test('empty risks, suggestions and keywords give an empty plan without errors', () => {
  const data = {
    scoreCapped: false,
    risks: [],
    suggestions: [],
    sectionScores: [],
  };

  assert.deepStrictEqual(buildActionPlan(data), []);
  assert.deepStrictEqual(buildActionPlan({}), []);
  assert.deepStrictEqual(buildActionPlan(null), []);
});

test('summaryFacts returns skills text, risk counts and resolved category ties by weight', () => {
  const data = {
    keywordMatchPercent: 75,
    sectionScores: [
      { sectionName: 'ATS_COMPATIBILITY', score: 85 },
      { sectionName: 'SKILLS_VALIDATION', score: 85, matchedKeywords: ['a', 'b', 'c'], missingKeywords: ['d'] },
      { sectionName: 'IMPACT_ACHIEVEMENTS', score: 70 },
      { sectionName: 'PROJECT_QUALITY', score: 70 },
      { sectionName: 'WRITING_QUALITY', score: 90 },
      { sectionName: 'PROFESSIONALISM', score: 60 },
    ],
    risks: [
      { code: 'NO_CONTACT_INFO', severity: 'HIGH' },
      { code: 'FILLER_WORDS', severity: 'LOW' },
    ],
  };

  const facts = summaryFacts(data);
  assert.strictEqual(facts.skillsCovered, '3 of 4 skills');
  assert.strictEqual(facts.percent, 75);
  assert.strictEqual(facts.riskCount, 2);
  assert.strictEqual(facts.highRiskCount, 1);
  assert.strictEqual(facts.strongestCategory, 'Writing quality');
  assert.strictEqual(facts.weakestCategory, 'Professionalism');
});

test('when both matched and missing keyword lists are empty, summaryFacts and plan do not show 0 of 0 skills or percent', () => {
  const data = {
    scoreCapped: false,
    overallScore: 80,
    uncappedScore: 80,
    skillsScore: 50,
    keywordMatchPercent: 0,
    sectionScores: [
      { sectionName: 'ATS_COMPATIBILITY', score: 100 },
      { sectionName: 'SKILLS_VALIDATION', score: 50, matchedKeywords: [], missingKeywords: [] },
      { sectionName: 'IMPACT_ACHIEVEMENTS', score: 100 },
      { sectionName: 'PROJECT_QUALITY', score: 50 },
      { sectionName: 'WRITING_QUALITY', score: 100 },
      { sectionName: 'PROFESSIONALISM', score: 100 },
    ],
    risks: [],
    suggestions: [],
  };

  const facts = summaryFacts(data);
  assert.strictEqual(facts.skillsCovered, 'Not scored');
  assert.strictEqual(facts.percent, null);
  assert.strictEqual(facts.keywordPercent, null);
  assert.ok(!facts.skillsCovered.includes('0 of 0'));

  const cap = capInfo(data);
  assert.strictEqual(cap, null);

  assert.strictEqual(verdictFor(data), QUALITY_ONLY_VERDICT);
  assert.strictEqual(verdictFor(data.overallScore, data), QUALITY_ONLY_VERDICT);

  const plan = buildActionPlan(data);
  assert.deepStrictEqual(plan, []);
  for (const item of plan) {
    assert.ok(!item.title.includes('0 of 0'));
    assert.ok(!item.why.includes('0 of 0'));
    assert.ok(!item.how.includes('0 of 0'));
    assert.ok(!item.gainText.includes('0 of 0'));
  }
});
