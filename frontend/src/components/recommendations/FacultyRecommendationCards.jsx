import { motion } from 'framer-motion';
import { Sparkles, Award } from 'lucide-react';

const MotionLi = motion.li;
import { cn } from '../../lib/utils';
import { Card, CardContent } from '../ui/Card';
import { Badge } from '../ui/Badge';

/**
 * Visual list of mentor recommendations (similarity + rank + progress bar).
 * Expects API shape: { fullName, expertise, similarityScore (0–1), facultyId }.
 */
export function FacultyRecommendationCards({ items, className }) {
  if (!items?.length) return null;

  const sorted = [...items].sort(
    (a, b) => (b.similarityScore || 0) - (a.similarityScore || 0)
  );

  return (
    <div className={cn('space-y-4', className)}>
      <div className="flex items-center gap-2 text-sm font-semibold text-mentor-text">
        <Sparkles className="h-4 w-4 text-mentor-primary" />
        Faculty recommendations
      </div>
      <ul className="space-y-3">
        {sorted.map((f, i) => {
          const pct = Math.min(100, Math.max(0, Math.round((f.similarityScore || 0) * 100)));
          return (
            <MotionLi
              key={f.facultyId ?? f.email ?? i}
              initial={{ opacity: 0, x: -8 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: i * 0.06, duration: 0.25 }}
            >
              <Card variant="glass" className="overflow-hidden">
                <CardContent className="space-y-3 py-4">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0">
                      <div className="flex flex-wrap items-center gap-2">
                        <span className="font-semibold text-mentor-text">{f.fullName || 'Faculty'}</span>
                        <Badge variant="primary" className="tabular-nums">
                          #{i + 1}
                        </Badge>
                      </div>
                      {f.expertise && (
                        <p className="mt-1 text-xs text-mentor-muted line-clamp-2">{f.expertise}</p>
                      )}
                    </div>
                    <div className="flex shrink-0 items-center gap-1 rounded-lg bg-mentor-primary/10 px-2 py-1 text-sm font-bold text-mentor-primary-dark">
                      <Award className="h-4 w-4" />
                      {pct}%
                    </div>
                  </div>
                  <div>
                    <div className="mb-1 flex justify-between text-xs text-mentor-muted">
                      <span>Similarity</span>
                      <span className="tabular-nums">{pct}%</span>
                    </div>
                    <div className="h-2.5 overflow-hidden rounded-full bg-mentor-border/80">
                      <motion.div
                        className="h-full rounded-full bg-gradient-to-r from-mentor-primary to-mentor-accent"
                        initial={{ width: 0 }}
                        animate={{ width: `${pct}%` }}
                        transition={{ duration: 0.7, delay: 0.15 + i * 0.05, ease: [0.22, 1, 0.36, 1] }}
                      />
                    </div>
                  </div>
                </CardContent>
              </Card>
            </MotionLi>
          );
        })}
      </ul>
    </div>
  );
}
