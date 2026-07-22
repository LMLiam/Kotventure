package io.github.lmliam.kotventure.coroutines.prompt

/**
 * A reusable question with typed options.
 *
 * A prompt is a template, not a component. [ask][io.github.lmliam.kotventure.coroutines.prompt.ask] runs [build] one
 * time for each audience that it asks. Therefore, a prompt that reads [PromptScope.viewer] shows different options to
 * different audiences.
 *
 * A prompt holds no lifetime. The caller of `ask` sets how long the buttons stay clickable.
 *
 * Declare a prompt as a value, as a class that carries its dependencies, or as an object.
 *
 * @param T the type that each option resumes with.
 * @param build appends the question text and its options to the prompt scope.
 * @sample io.github.lmliam.kotventure.coroutines.prompt.promptValueSample
 * @sample io.github.lmliam.kotventure.coroutines.prompt.promptObjectSample
 */
public open class Prompt<T>(
    internal val build: PromptScope<T>.() -> Unit,
)
