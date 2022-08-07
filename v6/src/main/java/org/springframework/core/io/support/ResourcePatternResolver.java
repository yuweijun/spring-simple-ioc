package org.springframework.core.io.support;

import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Strategy interface for resolving a location pattern into Resource objects.
 *
 * <p>Can be used with any sort of location pattern: Input patterns have
 * to match the strategy implementation. This interface just specifies the conversion method rather than a specific pattern format.
 *
 * @author Juergen Hoeller
 * @see PathMatchingResourcePatternResolver
 * @since 01.05.2004
 */
public interface ResourcePatternResolver {

    /**
     * Resolve the given location pattern into Resource objects.
     *
     * @param locationPattern the location pattern to resolve
     * @return the corresponding Resource objects
     * @throws IOException in case of I/O errors
     */
    Resource[] getResources(String locationPattern) throws IOException;

}
