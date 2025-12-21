import { Shield, Zap, Globe, FolderLock } from "lucide-react";

const features = [
    {
        name: "Secure Encryption",
        description: "Your files are encrypted at rest and in transit with enterprise-grade security standards.",
        icon: Shield,
    },
    {
        name: "Lightning Fast",
        description: "Experience blazing fast uploads and downloads with Pickbox.",
        icon: Zap,
    },
    {
        name: "Global Access",
        description: "Access your files from anywhere, on any device, at any time.",
        icon: Globe,
    },
    {
        name: "Organized Storage",
        description: "Smart categorization and powerful search make finding your files a breeze.",
        icon: FolderLock,
    },
];

export function Features() {
    return (
        <div className="py-24 sm:py-32">
            <div className="mx-auto max-w-7xl px-6 lg:px-8">
                <div className="mx-auto max-w-2xl lg:text-center">
                    <h2 className="text-base font-semibold leading-7 text-primary">Deploy faster</h2>
                    <p className="mt-2 text-3xl font-bold tracking-tight text-foreground sm:text-4xl">
                        Everything you need for secure storage
                    </p>
                    <p className="mt-6 text-lg leading-8 text-muted-foreground">
                        Pickbox combines security, speed, and simplicity to give you the best file management experience possible.
                    </p>
                </div>
                <div className="mx-auto mt-16 max-w-2xl sm:mt-20 lg:mt-24 lg:max-w-4xl">
                    <dl className="grid max-w-xl grid-cols-1 gap-x-8 gap-y-10 lg:max-w-none lg:grid-cols-2 lg:gap-y-16">
                        {features.map((feature) => (
                            <div key={feature.name} className="relative pl-16">
                                <dt className="text-base font-semibold leading-7 text-foreground">
                                    <div className="absolute left-0 top-0 flex h-10 w-10 items-center justify-center rounded-lg bg-primary">
                                        <feature.icon className="h-6 w-6 text-primary-foreground" aria-hidden="true" />
                                    </div>
                                    {feature.name}
                                </dt>
                                <dd className="mt-2 text-base leading-7 text-muted-foreground">
                                    {feature.description}
                                </dd>
                            </div>
                        ))}
                    </dl>
                </div>
            </div>
        </div>
    );
}
