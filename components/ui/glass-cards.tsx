import React, { useEffect, useRef, useState } from 'react';
import { gsap } from 'gsap';
import { ScrollTrigger } from 'gsap/ScrollTrigger';
import { cardData } from '../../lib/utils';

gsap.registerPlugin(ScrollTrigger);

interface CardProps {
    id: number;
    title: string;
    description: string;
    index: number;
    totalCards: number;
    color: string;
}

const Card: React.FC<CardProps> = ({ title, description, index, totalCards, color }) => {
    const cardRef = useRef<HTMLDivElement>(null);
    const containerRef = useRef<HTMLDivElement>(null);

    useEffect(() => {
        const card = cardRef.current;
        const container = containerRef.current;
        if (!card || !container) return;

        const targetScale = 1 - (totalCards - index) * 0.05;

        // Set initial state
        gsap.set(card, {
            scale: 1,
            transformOrigin: "center top"
        });

        // Create scroll trigger for stacking effect (similar to the reference component)
        ScrollTrigger.create({
            trigger: container,
            start: "top center",
            end: "bottom center",
            scrub: 1,
            onUpdate: (self) => {
                const progress = self.progress;
                const scale = gsap.utils.interpolate(1, targetScale, progress);

                gsap.set(card, {
                    scale: Math.max(scale, targetScale),
                    transformOrigin: "center top"
                });
            }
        });

        return () => {
            ScrollTrigger.getAll().forEach(trigger => trigger.kill());
        };
    }, [index, totalCards]);

    return (
        <div
            ref={containerRef}
            style={{
                height: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                position: 'sticky',
                top: 0
            }}
        >
            <div
                ref={cardRef}
                style={{
                    position: 'relative',
                    width: '70%',
                    height: '450px',
                    borderRadius: '24px',
                    isolation: 'isolate',
                    top: `calc(-5vh + ${index * 25}px)`,
                    transformOrigin: 'top'
                }}
                className="card-content"
            >
                {/* Electric Border Effect using Tailwind/CSS variables with fallback */}
                <div
                    style={{
                        position: 'absolute',
                        inset: '-3px',
                        borderRadius: '27px',
                        padding: '3px',
                        background: `conic-gradient(
                            from 0deg,
                            transparent 0deg,
                            var(--theme-primary, ${color}) 60deg,
                            var(--theme-secondary, ${color.replace('0.8', '0.6')}) 120deg,
                            transparent 180deg,
                            var(--theme-secondary, ${color.replace('0.8', '0.4')}) 240deg,
                            transparent 360deg
                        )`,
                        zIndex: -1
                    }}
                />

                {/* Main Card Content */}
                <div style={{
                    position: 'relative',
                    width: '100%',
                    height: '100%',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'center',
                    padding: '3rem',
                    borderRadius: '24px',
                    background: `
                        linear-gradient(145deg, 
                            rgba(255, 255, 255, 0.1), 
                            rgba(255, 255, 255, 0.05)
                        )
                    `,
                    backdropFilter: 'blur(25px) saturate(180%)',
                    border: '1px solid rgba(255, 255, 255, 0.2)',
                    boxShadow: `
                        0 8px 32px rgba(0, 0, 0, 0.3),
                        0 2px 8px rgba(0, 0, 0, 0.2),
                        inset 0 1px 0 rgba(255, 255, 255, 0.3),
                        inset 0 -1px 0 rgba(255, 255, 255, 0.1)
                    `,
                    overflow: 'hidden'
                }}>
                    {/* Enhanced Glass reflection overlay */}
                    <div style={{
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        right: 0,
                        height: '60%',
                        background: 'linear-gradient(135deg, rgba(255, 255, 255, 0.25) 0%, rgba(255, 255, 255, 0.1) 50%, transparent 100%)',
                        pointerEvents: 'none',
                        borderRadius: '24px 24px 0 0'
                    }} />

                    {/* Glass shine effect */}
                    <div style={{
                        position: 'absolute',
                        top: '10px',
                        left: '10px',
                        right: '10px',
                        height: '2px',
                        background: 'linear-gradient(90deg, transparent 0%, rgba(255, 255, 255, 0.6) 50%, transparent 100%)',
                        borderRadius: '1px',
                        pointerEvents: 'none'
                    }} />

                    {/* Side glass reflection */}
                    <div style={{
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        width: '2px',
                        height: '100%',
                        background: 'linear-gradient(180deg, rgba(255, 255, 255, 0.3) 0%, transparent 50%)',
                        borderRadius: '24px 0 0 24px',
                        pointerEvents: 'none'
                    }} />

                    {/* Content text */}
                    <div style={{ position: 'relative', zIndex: 10 }}>
                        <h2 style={{
                            fontSize: '2rem',
                            fontWeight: '700',
                            marginBottom: '1rem',
                            color: '#ffffff',
                            textShadow: '0 2px 4px rgba(0,0,0,0.5)'
                        }}>{title}</h2>
                        <p style={{
                            fontSize: '1.1rem',
                            lineHeight: '1.6',
                            color: 'rgba(255, 255, 255, 0.85)',
                            maxWidth: '600px'
                        }}>{description}</p>
                    </div>

                    {/* Frosted glass texture */}
                    <div style={{
                        position: 'absolute',
                        top: 0,
                        left: 0,
                        right: 0,
                        bottom: 0,
                        backgroundImage: `
                            radial-gradient(circle at 20% 30%, rgba(255,255,255,0.1) 1px, transparent 2px),
                            radial-gradient(circle at 80% 70%, rgba(255,255,255,0.08) 1px, transparent 2px),
                            radial-gradient(circle at 40% 80%, rgba(255,255,255,0.06) 1px, transparent 2px)
                        `,
                        backgroundSize: '30px 30px, 25px 25px, 35px 35px',
                        pointerEvents: 'none',
                        borderRadius: '24px',
                        opacity: 0.7
                    }} />
                </div>
            </div>
        </div>
    );
};

// Liquid Glass Color Presets structure
const presetsInfo: Record<string, {
    name: string;
    primary: string;
    secondary: string;
    background: string;
    glow: string;
}> = {
    MIDNIGHT: {
        name: "Midnight Obsidian",
        primary: "rgba(157, 78, 221, 0.8)",
        secondary: "rgba(90, 24, 154, 0.8)",
        background: "linear-gradient(135deg, #050b14 0%, #1a172e 100%)",
        glow: "rgba(157, 78, 221, 0.4)"
    },
    ARCTIC: {
        name: "Arctic Glacier",
        primary: "rgba(142, 202, 230, 0.8)",
        secondary: "rgba(33, 158, 188, 0.8)",
        background: "linear-gradient(135deg, #0f151b 0%, #1c242c 100%)",
        glow: "rgba(142, 202, 230, 0.4)"
    },
    OCEAN: {
        name: "Abyssal Ocean",
        primary: "rgba(0, 180, 216, 0.8)",
        secondary: "rgba(3, 4, 94, 0.8)",
        background: "linear-gradient(135deg, #050b14 0%, #0b1724 100%)",
        glow: "rgba(0, 180, 216, 0.4)"
    },
    AURORA: {
        name: "Aurora Glow",
        primary: "rgba(0, 255, 204, 0.8)",
        secondary: "rgba(0, 150, 136, 0.8)",
        background: "linear-gradient(135deg, #050e0c 0%, #0a1c18 100%)",
        glow: "rgba(0, 255, 204, 0.4)"
    },
    EMERALD: {
        name: "Emerald Forest",
        primary: "rgba(16, 185, 129, 0.8)",
        secondary: "rgba(4, 120, 87, 0.8)",
        background: "linear-gradient(135deg, #080c0b 0%, #0f1715 100%)",
        glow: "rgba(16, 185, 129, 0.4)"
    }
};

interface ControlPanelProps {
    currentPreset: string;
    onPresetChange: (key: string) => void;
}

export const LiquidThemeControlPanel: React.FC<ControlPanelProps> = ({ currentPreset, onPresetChange }) => {
    const keys = Object.keys(presetsInfo);
    const currentIndex = keys.indexOf(currentPreset);
    const [isAutoPlaying, setIsAutoPlaying] = useState(false);
    const timerRef = useRef<NodeJS.Timeout | null>(null);

    const handleNext = () => {
        const nextIndex = (currentIndex + 1) % keys.length;
        onPresetChange(keys[nextIndex]);
    };

    const handlePrev = () => {
        const prevIndex = (currentIndex - 1 + keys.length) % keys.length;
        onPresetChange(keys[prevIndex]);
    };

    // Auto playing cycle mechanism
    useEffect(() => {
        if (isAutoPlaying) {
            timerRef.current = setInterval(() => {
                handleNext();
            }, 3000);
        } else {
            if (timerRef.current) clearInterval(timerRef.current);
        }

        return () => {
            if (timerRef.current) clearInterval(timerRef.current);
        };
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [isAutoPlaying]);

    const activePreset = presetsInfo[currentPreset];

    return (
        <div style={{
            position: 'fixed',
            bottom: '30px',
            right: '30px',
            zIndex: 1000,
            width: '320px',
            fontFamily: 'system-ui, -apple-system, sans-serif',
            userSelect: 'none'
        }}>
            {/* Translucent Glass Card matching requested rules: 20-30px corner radius, soft border */}
            <div style={{
                background: 'linear-gradient(135deg, rgba(255, 255, 255, 0.12) 0%, rgba(255, 255, 255, 0.04) 100%)',
                backdropFilter: 'blur(20px) saturate(180%)',
                border: '1px solid rgba(255, 255, 255, 0.2)',
                borderRadius: '24px', // 20-30px corner radius
                padding: '20px',
                boxShadow: '0 20px 45px rgba(0, 0, 0, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.2)',
                display: 'flex',
                flexDirection: 'column',
                gap: '14px',
                color: '#ffffff',
                transition: 'all 0.4s ease'
            }}>
                {/* Header info */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', flexDirection: 'column' }}>
                        <span style={{ fontSize: '0.65rem', fontWeight: 800, color: 'rgba(255,255,255,0.4)', letterSpacing: '1.5px', textTransform: 'uppercase' }}>
                            Engine Presets
                        </span>
                        <span style={{ fontSize: '1.1rem', fontWeight: 700, letterSpacing: '-0.3px', marginTop: '2px' }}>
                            {activePreset.name}
                        </span>
                    </div>
                    {/* Auto playing badge status */}
                    <button 
                        onClick={() => setIsAutoPlaying(!isAutoPlaying)}
                        style={{
                            background: isAutoPlaying ? 'rgba(0, 255, 204, 0.2)' : 'rgba(255, 255, 255, 0.08)',
                            border: `1px solid ${isAutoPlaying ? '#00ffcc' : 'rgba(255, 255, 255, 0.15)'}`,
                            color: isAutoPlaying ? '#00ffcc' : 'rgba(255, 255, 255, 0.7)',
                            borderRadius: '80px',
                            padding: '4px 10px',
                            fontSize: '0.65rem',
                            fontWeight: '700',
                            cursor: 'pointer',
                            display: 'flex',
                            alignItems: 'center',
                            gap: '4px',
                            textTransform: 'uppercase',
                            transition: 'all 0.3s'
                        }}
                    >
                        <span style={{
                            width: '6px',
                            height: '6px',
                            borderRadius: '50%',
                            background: isAutoPlaying ? '#00ffcc' : 'rgba(255, 255, 255, 0.4)',
                            display: 'inline-block',
                            animation: isAutoPlaying ? 'pulse 1.5s infinite' : 'none'
                        }} />
                        {isAutoPlaying ? 'LOOP ON' : 'AUTO'}
                    </button>
                </div>

                {/* Theme CSS variables visualization bar */}
                <div style={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '4px',
                    background: 'rgba(0,0,0,0.2)',
                    padding: '8px 12px',
                    borderRadius: '12px',
                    fontSize: '0.7rem',
                    fontFamily: 'monospace',
                    border: '1px solid rgba(255,255,255,0.05)'
                }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: 'rgba(255,255,255,0.4)' }}>--theme-primary:</span>
                        <span style={{ color: activePreset.primary }}>{activePreset.primary.replace(/\s+/g, '')}</span>
                    </div>
                    <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                        <span style={{ color: 'rgba(255,255,255,0.4)' }}>--theme-glow:</span>
                        <span style={{ color: activePreset.glow }}>{activePreset.glow.replace(/\s+/g, '')}</span>
                    </div>
                </div>

                {/* Color swatches navigation row */}
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div style={{ display: 'flex', gap: '8px' }}>
                        {keys.map((key) => {
                            const preset = presetsInfo[key];
                            const isSelected = currentPreset === key;
                            return (
                                <button
                                    key={key}
                                    onClick={() => onPresetChange(key)}
                                    style={{
                                        width: '22px',
                                        height: '22px',
                                        borderRadius: '50%',
                                        background: preset.primary,
                                        border: isSelected ? '2px solid #ffffff' : '1.5px solid rgba(255,255,255,0.25)',
                                        cursor: 'pointer',
                                        padding: 0,
                                        boxShadow: isSelected ? `0 0 10px ${preset.primary}` : 'none',
                                        transform: isSelected ? 'scale(1.15)' : 'scale(1)',
                                        transition: 'all 0.2s cubic-bezier(0.175, 0.885, 0.32, 1.275)'
                                    }}
                                    title={preset.name}
                                />
                            );
                        })}
                    </div>

                    {/* Step Controls */}
                    <div style={{ display: 'flex', gap: '6px' }}>
                        <button
                            onClick={handlePrev}
                            style={{
                                background: 'rgba(255, 255, 255, 0.08)',
                                color: '#ffffff',
                                border: '1px solid rgba(255, 255, 255, 0.1)',
                                cursor: 'pointer',
                                borderRadius: '8px',
                                width: '28px',
                                height: '28px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '0.8rem',
                                transition: 'all 0.2s'
                            }}
                            title="Previous Preset"
                        >
                            ◀
                        </button>
                        <button
                            onClick={handleNext}
                            style={{
                                background: 'rgba(255, 255, 255, 0.08)',
                                color: '#ffffff',
                                border: '1px solid rgba(255, 255, 255, 0.1)',
                                cursor: 'pointer',
                                borderRadius: '8px',
                                width: '28px',
                                height: '28px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                fontSize: '0.8rem',
                                transition: 'all 0.2s'
                            }}
                            title="Next Preset"
                        >
                            ▶
                        </button>
                    </div>
                </div>
            </div>

            {/* Custom pulse keyframes inlined safely via style tag */}
            <style>{`
                @keyframes pulse {
                    0% { transform: scale(1); opacity: 1; }
                    50% { transform: scale(1.3); opacity: 0.7; }
                    100% { transform: scale(1); opacity: 1; }
                }
            `}</style>
        </div>
    );
};

export const StackedCards: React.FC = () => {
    const containerRef = useRef<HTMLDivElement>(null);
    const [currentPreset, setCurrentPreset] = useState<string>("MIDNIGHT");

    useEffect(() => {
        const container = containerRef.current;
        if (!container) return;

        gsap.fromTo(container,
            { opacity: 0 },
            {
                opacity: 1,
                duration: 1.2,
                ease: "power2.out"
            }
        );
    }, []);

    // Set properties mapping presets to CSS custom variables (allowing Tailwind integration)
    const active = presetsInfo[currentPreset] || presetsInfo.MIDNIGHT;
    const themeStyles = {
        '--theme-primary': active.primary,
        '--theme-secondary': active.secondary,
        '--theme-bg': active.background,
        '--theme-glow': active.glow,
        background: active.background,
        transition: "background 0.8s ease-in-out"
    } as React.CSSProperties;

    return (
        <main ref={containerRef} style={themeStyles}>
            {/* Beautiful Liquid Glass Floating Control Panel */}
            <LiquidThemeControlPanel 
                currentPreset={currentPreset}
                onPresetChange={(key) => setCurrentPreset(key)}
            />

            {/* Hero Section */}
            <section style={{
                height: '70vh',
                width: '100%',
                display: 'grid',
                placeContent: 'center',
                position: 'relative',
                color: '#ffffff'
            }}>
                <div style={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    backgroundImage: `
                        linear-gradient(to right, rgba(79, 79, 79, 0.18) 1px, transparent 1px),
                        linear-gradient(to bottom, rgba(79, 79, 79, 0.18) 1px, transparent 1px)
                    `,
                    backgroundSize: '54px 54px',
                    maskImage: 'radial-gradient(ellipse 60% 50% at 50% 0%, #000 70%, transparent 100%)'
                }} />
                <h1 style={{
                    fontSize: 'clamp(2rem, 5vw, 4rem)',
                    fontWeight: '500',
                    textAlign: 'center',
                    lineHeight: '1.2',
                    padding: '0 2rem',
                    position: 'relative',
                    zIndex: 1
                }}>
                    Stacking Glass Cards with GSAP <br /> Scroll down! 👇
                </h1>
            </section>

            {/* Cards Section */}
            <section style={{
                color: '#ffffff',
                width: '100%'
            }}>
                {cardData.map((card, index) => {
                    return (
                        <Card
                            key={card.id}
                            id={card.id}
                            title={card.title}
                            description={card.description}
                            index={index}
                            totalCards={cardData.length}
                            color={card.color}
                        />
                    );
                })}
            </section>
        </main>
    );
};
