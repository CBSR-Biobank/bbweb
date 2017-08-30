/**
 * Jasmine test suite
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
define(function (require) {
  'use strict';

  var mocks = require('angularMocks'),
      _     = require('lodash');

  describe('Component: entityTimestamps', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (timeAdded, timeModified) {
        timeAdded = timeAdded || new Date();
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<entity-timestamps ',
            '  time-added="vm.timeAdded" ',
            '  time-modified="vm.timeModified">',
            '</entity-timestamps>'
          ].join(''),
          {
            timeAdded:    timeAdded,
            timeModified: timeModified
          },
          'entityTimestamps');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope', '$compile');
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/entityTimestamps/entityTimestamps.html');
    }));

    it('timeAdded is valid', function() {
      var timeAdded = new Date();
      this.createController(timeAdded);
      expect(this.controller.timeAdded).toBe(timeAdded);
    });

    it('timeAdded is undefined if initial value before year 2000', function() {
      var timeAdded = new Date(1900, 1);
      this.createController(timeAdded);
      expect(this.controller.timeAdded).toBeUndefined();
    });

    it('timeModified is valid', function() {
      var timeModified = new Date();
      this.createController(null, timeModified);
      expect(this.controller.timeModified).toBe(timeModified);
    });

  });

});
