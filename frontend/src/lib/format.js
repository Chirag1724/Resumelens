export function formatScore(n) {
  const num = Number(n);
  if (!Number.isFinite(num)) {
    return '0';
  }
  const rounded = Math.round((num + Number.EPSILON) * 10) / 10;
  if (rounded === 0) {
    return '0';
  }
  return rounded % 1 === 0 ? String(rounded) : rounded.toFixed(1);
}

export default formatScore;

