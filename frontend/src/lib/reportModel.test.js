import test from 'node:test';
import assert from 'node:assert/strict';

import {
  verdictFor,
  capInfo,
  pointsGain,
  pointsEarned,
  keywordBand,
  summaryFacts,
  buildActionPlan,
} from './reportModel.js';
import { VERDICTS } from '../content/reportCopy.js';

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

test('with skills 16, 3 matched and 16 missing, threshold is 40 and moreSkills is 5', () => {
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
});

test('with skills 50, 10 matched and 10 missing, threshold is 60 and moreSkills is 2', () => {
  const data = {
    scoreCapped: true,
    overallScore: 69,
    uncappedScore: 78.4,
    skillsScore: 50,
    sectionScores: [
      {
        sectionName: 'SKILLS_VALIDATION',
        score: 50,
        matchedKeywords: ['k1', 'k2', 'k3', 'k4', 'k5', 'k6', 'k7', 'k8', 'k9', 'k10'],
        missingKeywords: ['m1', 'm2', 'm3', 'm4', 'm5', 'm6', 'm7', 'm8', 'm9', 'm10'],
      },
    ],
  };

  const info = capInfo(data);
  assert.notStrictEqual(info, null);
  assert.strictEqual(info.threshold, 60);
  assert.strictEqual(info.moreSkills, 2);
  assert.strictEqual(info.cap, 69);
  assert.strictEqual(info.uncapped, 78.4);
  assert.strictEqual(info.skillsNow, 50);
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
  assert.strictEqual(skillsItems[0].fromResume, 'Built services with Spring and Docker.');
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

test('pointsEarned(85, 25) is 21.3 of 25 (21.25 rounds to 21.3)', () => {
  assert.strictEqual(pointsEarned(85, 25), '21.3 of 25');
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

