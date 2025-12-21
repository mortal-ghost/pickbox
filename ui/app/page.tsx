import Image from "next/image";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import { Navbar } from "@/components/landing/navbar";
import { Features } from "@/components/landing/features";

export default function Home() {
  return (
    <div className="flex min-h-screen flex-col bg-background">
      <Navbar />

      <main className="flex-1">
        {/* Hero Section */}
        <section className="relative overflow-hidden py-20 sm:py-32 lg:pb-32 xl:pb-36">
          <div className="container mx-auto px-4">
            <div className="lg:grid lg:grid-cols-12 lg:gap-x-8 lg:gap-y-20">

              {/* Left Column: Text */}
              <div className="relative z-10 mx-auto max-w-2xl lg:col-span-7 lg:max-w-none lg:pt-6 xl:col-span-6">
                <h1 className="text-4xl font-bold tracking-tight text-foreground sm:text-6xl">
                  Secure storage for the modern web
                </h1>
                <p className="mt-6 text-lg leading-8 text-muted-foreground">
                  Pickbox provides a seamless, secure, and lightning-fast way to store and share your files.
                  Experience the future of cloud storage with our intuitive platform.
                </p>
                <div className="mt-8 flex flex-wrap gap-x-6 gap-y-4">
                  <Button size="lg" asChild>
                    <Link href="/auth?mode=register">Get Started</Link>
                  </Button>
                  <Button variant="outline" size="lg" asChild>
                    <Link href="/auth?mode=login">Live Demo</Link>
                  </Button>
                </div>
              </div>

              {/* Right Column: Image */}
              <div className="relative mt-16 lg:col-span-5 lg:mt-0 xl:col-span-6">
                <div className="relative rounded-xl bg-gray-900/5 p-2 ring-1 ring-inset ring-gray-900/10 dark:bg-gray-50/5 dark:ring-gray-50/10 lg:-m-4 lg:p-4">
                  <Image
                    src="/hero-dashboard.png"
                    alt="Pickbox Dashboard Interface"
                    width={1000}
                    height={1000}
                    className="w-full rounded-md shadow-2xl ring-1 ring-gray-900/10 dark:ring-gray-50/10"
                    priority
                  />
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Features Section */}
        <Features />
      </main>

      {/* Footer */}
      <footer className="border-t py-6 md:py-0 ml-4">
        <div className="container flex flex-col items-center justify-between gap-4 md:h-24 md:flex-row">
          <p className="text-center text-sm leading-loose text-muted-foreground md:text-left">
            © {new Date().getFullYear()} Pickbox. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
}
