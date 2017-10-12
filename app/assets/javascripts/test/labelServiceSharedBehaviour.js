/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */
define(['lodash'], function(_) {
  'use strict';

  function labelServiceSharedBehaviour(context) {

    it('has valid values', function() {
      _.values(context.labels).forEach(function (state) {
        expect(context.toLabelFunc(state)()).toBe(context.expectedLabels[state]);
      });
    });

    it('throws error when invalid state is used', function() {
      var self = this;
      this.injectDependencies('Factory');
      expect(function () {
        context.toLabelFunc(self.Factory.stringNext());
      }).toThrowError(/no such label:/);
    });

  }

  return labelServiceSharedBehaviour;
});
