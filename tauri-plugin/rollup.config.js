import { readFileSync } from 'node:fs'
import { dirname } from 'node:path'
import typescript from '@rollup/plugin-typescript'

const pkg = JSON.parse(readFileSync(new URL('./package.json', import.meta.url), 'utf8'))

/** @type {import('rollup').RollupOptions} */
export default {
  input: 'guest-js/index.ts',
  output: [
    {
      file: pkg.exports.import,
      format: 'esm'
    },
    {
      file: pkg.exports.require,
      format: 'cjs'
    }
  ],
  plugins: [
    typescript({
      declaration: true,
      declarationDir: dirname(pkg.exports.import)
    })
  ],
  external: [
    /^@tauri-apps\/api/,
    ...Object.keys(pkg.dependencies || {}),
    ...Object.keys(pkg.peerDependencies || {})
  ]
}
