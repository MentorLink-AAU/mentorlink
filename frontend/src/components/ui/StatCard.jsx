import { useEffect, useRef } from 'react';
import { motion, animate } from 'framer-motion';
import { cn } from '../../lib/utils';
import { Card, CardContent } from './Card';

const MotionDiv = motion.div;

/**
 * Analytics-style stat tile with optional animated count and glass styling.
 */
export function StatCard({
  icon,
  label,
  value,
  suffix = '',
  index = 0,
  glass = true,
  className,
}) {
  const displayRef = useRef(null);
  const numeric = typeof value === 'number' ? value : Number(value) || 0;

  useEffect(() => {
    const el = displayRef.current;
    if (!el) return;
    const controls = animate(0, numeric, {
      duration: 1.05,
      delay: index * 0.06,
      ease: [0.22, 1, 0.36, 1],
      onUpdate: (v) => {
        el.textContent = `${Math.round(v)}${suffix}`;
      },
    });
    return () => controls.stop();
  }, [numeric, suffix, index]);

  const IconComponent = icon;

  return (
    <MotionDiv
      initial={{ opacity: 0, y: 14 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.35, delay: index * 0.05, ease: [0.22, 1, 0.36, 1] }}
      whileHover={{ y: -3, transition: { duration: 0.2 } }}
      className={cn(className)}
    >
      <Card variant={glass ? 'glass' : 'default'} className="h-full overflow-hidden">
        <CardContent className="flex items-center gap-4 py-5">
          <div
            className={cn(
              'rounded-xl p-3',
              glass ? 'bg-mentor-primary/15 text-mentor-primary' : 'bg-mentor-primary/10 text-mentor-primary'
            )}
          >
            <IconComponent className="h-6 w-6" />
          </div>
          <div className="min-w-0 flex-1">
            <p
              ref={displayRef}
              className="text-2xl font-bold tracking-tight text-mentor-text tabular-nums"
            >
              0{suffix}
            </p>
            <p className="text-sm text-mentor-muted">{label}</p>
          </div>
        </CardContent>
      </Card>
    </MotionDiv>
  );
}
