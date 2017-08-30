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

  describe('Component: stateAndTimestamps', function() {

    function SuiteMixinFactory(ComponentTestSuiteMixin) {

      function SuiteMixin() {
        ComponentTestSuiteMixin.call(this);
      }

      SuiteMixin.prototype = Object.create(ComponentTestSuiteMixin.prototype);
      SuiteMixin.prototype.constructor = SuiteMixin;

      SuiteMixin.prototype.createController = function (timeAdded, timeModified) {
        timeAdded = timeAdded || new Date();
        this.stateLabelFunc = jasmine.createSpy().and.returnValue(null);
        ComponentTestSuiteMixin.prototype.createController.call(
          this,
          [
            '<state-and-timestamps ',
            '  state-label-func="vm.stateLabelFunc" ',
            '  time-added="vm.timeAdded" ',
            '  time-modified="vm.timeModified">',
            '</state-and-timestamps>'
          ].join(''),
          {
            stateLabelFunc: this.stateLabelFunc,
            timeAdded:      timeAdded,
            timeModified:   timeModified
          },
          'stateAndTimestamps');
      };

      return SuiteMixin;
    }

    beforeEach(mocks.module('biobankApp', 'biobank.test'));

    beforeEach(inject(function(ComponentTestSuiteMixin) {
      _.extend(this, new SuiteMixinFactory(ComponentTestSuiteMixin).prototype);

      this.injectDependencies('$rootScope', '$compile');
      this.putHtmlTemplates(
        '/assets/javascripts/common/components/stateAndTimestamps/stateAndTimestamps.html',
        '/assets/javascripts/common/components/entityTimestamps/entityTimestamps.html');
    }));

    it('scope is valid', function() {
      var timeAdded = new Date(),
          timeModified = new Date();
      this.createController(timeAdded, timeModified);
      expect(this.controller.stateLabelFunc).toBeFunction();
      expect(this.controller.timeAdded).toBe(timeAdded);
      expect(this.controller.timeModified).toBe(timeModified);
    });

  });

});
