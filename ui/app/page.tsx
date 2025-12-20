import Image from "next/image";

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center">
      <main className="flex flex-col items-center gap-8">
        <div className="relative h-32 w-32">
          <Image
            src="/logo.svg"
            alt="Pickbox Logo"
            fill
            className="object-contain"
            priority
          />
        </div>
        <h1 className="text-4xl font-bold tracking-tight text-foreground sm:text-6xl">
          Pickbox
        </h1>
        <p className="text-lg text-muted-foreground">
          Your modern file storage solution.
        </p>
      </main>
    </div>
  );
}
