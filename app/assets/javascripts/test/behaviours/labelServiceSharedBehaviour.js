/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

export default function labelServiceSharedBehaviour(context) {

  it('has valid values', function() {
    Object.values(context.labels).forEach(state => {
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
