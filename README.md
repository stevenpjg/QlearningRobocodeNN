#QlearningRobocodeNN

This code trains a robot in a robocode environment using Reinforcement Learning approach in specific Q-learning algorithm. (For more information about robocode see [Robocode](http://robocode.sourceforge.net/). 

Check the video here for more information on the code: [video](https://youtu.be/qZd7ptXNIkI).

## Overview

## LookUpTable

The LookUpTable folder contains the robot `Rl_check.java` which you can learn using a Look up Table (state-action and corresponding Q-values learnt in a look up table). The `Rl_check.data` folder contains the Look up table that is being trained (`LookUpTable.txt`). For initialization you can use the `LookupTable.txt` in the `zero` folder. If you want a trained look up table you can use `LookUpTable.txt` in the `learnt99000` folder. (The RL_check robot was trained against spinbot for 99000 battles). The states are quantized since robocode can store a table of maximum lenght 10,000.

## NeuralNetwork
The NeuralNetwork folder contains the robot `Rl_nn.java` which you can learn Q-values of a robot using neural networks (i.e. state-action and corresponding Q-values are learnt using look up table). The `Rl_nn.data` folder contains the weights being trained in `weights_hidden.txt` (hidden layer weights) and `weights_output.txt` (output layer weights). While using this robot for training use the weights from pretrained weights as it was trained separately on a batch mode. The trained robot can be found in folder labeled with numbers. The number denotes number of battles used to do the training.  

**Note**: The neural network learns Q-value and the corresponding state action pair online.

## Getting Started
1. Install Robocode (check [link](http://robocode.sourceforge.net/) for installation )
2. Make sure you include your robocode environment variable in your IDE
3. Put the robots either `Rl_check.java` or `Rl_check.java` in the `robot` folder (The robot folder can be found under the directory where robocode is installed)
4. Start the battle with the one of the RL robot and choose an enemy robot. 
5. While training set the explore boolean variable to true
6. Run for atleast 15,000 battles (atleast 50,000 in case of neural networks)
7. While testing set the greedy boolean variable to true
8. Win the enemy with your trained robot!

## Dependencies
- Robocode
- Java
- Eclipse (or other IDE, make sure you have added Robocode environment variable)
**Note**: Change the package name to the one you have created.
