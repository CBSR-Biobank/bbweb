import 'angular';
import 'angular-mocks/angular-mocks';
import TestModule from './test';
import biobankApp from './app';

const context = require.context('./', true, /Spec\.js$/);

context.keys().forEach(context);
