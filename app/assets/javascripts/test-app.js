import 'angular';
import 'angular-mocks/angular-mocks';
import TestModule from './test';       // eslint-disable-line no-unused-vars
import biobankApp from './app';        // eslint-disable-line no-unused-vars

const context = require.context('./', true, /Spec\.js$/);

context.keys().forEach(context);
