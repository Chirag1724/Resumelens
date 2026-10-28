export const SCORE_TIERS = {
  EXCELLENT: {
    tier: 'EXCELLENT',
    label: 'Excellent Match',
    color: '#00a46d',
    token: 'var(--score-excellent)',
  },
  STRONG: {
    tier: 'STRONG',
    label: 'Strong Match',
    color: '#6fcf97',
    token: 'var(--score-strong)',
  },
  MODERATE: {
    tier: 'MODERATE',
    label: 'Moderate Match',
    color: '#fbc904',
    token: 'var(--score-moderate)',
  },
  WEAK: {
    tier: 'WEAK',
    label: 'Weak Match',
    color: '#ff9b00',
    token: 'var(--score-weak)',
  },
  POOR: {
    tier: 'POOR',
    label: 'Poor Match',
    color: '#ef4444',
    token: 'var(--score-poor)',
  },
};

export function getScoreDetails(scoreOrTier) {
  if (typeof scoreOrTier === 'string' && SCORE_TIERS[scoreOrTier.toUpperCase()]) {
    return SCORE_TIERS[scoreOrTier.toUpperCase()];
  }
  const num = Number(scoreOrTier);
  const validNum = Number.isFinite(num) ? num : 0;
  if (validNum >= 85) return SCORE_TIERS.EXCELLENT;
  if (validNum >= 70) return SCORE_TIERS.STRONG;
  if (validNum >= 55) return SCORE_TIERS.MODERATE;
  if (validNum >= 40) return SCORE_TIERS.WEAK;
  return SCORE_TIERS.POOR;
}

export default getScoreDetails;
