'use strict';

const fs = require('fs');
const { collect } = require('./collect.js');

module.exports = async function run({ github, context, core }) {
  const result = await collect({ env: process.env, context, github, core });
  if (result === null) return;
  const outputPath = process.env.OUTPUT_PATH;
  fs.writeFileSync(outputPath, JSON.stringify(result), 'utf8');
  core.info(`Wrote PR metrics result to ${outputPath}`);
};
